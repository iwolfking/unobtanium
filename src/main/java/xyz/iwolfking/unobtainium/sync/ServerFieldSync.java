package xyz.iwolfking.unobtainium.sync;

import io.netty.buffer.Unpooled;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.GenericFieldKey;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.data.sync.context.ClientSyncContext;
import iskallia.vault.core.vault.Vault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import xyz.iwolfking.unobtainium.Unobtanium;

/**
 * per-tick delta sync of the vault's fields to players. each field is serialized to a long[] and
 * diffed against a retained baseline; veterans receive only changed fields, newcomers a full keyframe.
 */
public final class ServerFieldSync {

    private ServerFieldSync() {
    }

    public static final class State {
        long[][] baseline = new long[0][];
        int[] baselineLen = new int[0];
        final Set<UUID> synced = new HashSet<>();

        // reusable per-tick working state
        private final Set<UUID> eligibleIds = new HashSet<>();
        private final ReusableBitBuffer scratch = new ReusableBitBuffer();
        private final FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        // changed-field section of the delta (idx + LongArrayDelta), staged during collect while
        // both the new value and the old baseline are live, then spliced into the packet in writeDelta.
        private final FriendlyByteBuf deltaStage = new FriendlyByteBuf(Unpooled.buffer());
        private boolean[] seen = new boolean[0];

        private int changedCount;
        private int[] removedIdx = new int[16];
        private int removedCount;

        void invalidate() {
            this.baseline = new long[0][];
            this.baselineLen = new int[0];
            this.seen = new boolean[0];
            this.synced.clear();
        }

        private void ensureIndex(int idx) {
            if (idx >= this.baseline.length) {
                int n = Math.max(idx + 1, Math.max(4, this.baseline.length * 2));
                this.baseline = Arrays.copyOf(this.baseline, n);
                this.baselineLen = Arrays.copyOf(this.baselineLen, n);
                this.seen = Arrays.copyOf(this.seen, n);
            }
        }

        private void addRemoved(int idx) {
            if (this.removedCount == this.removedIdx.length) {
                this.removedIdx = Arrays.copyOf(this.removedIdx, this.removedCount * 2);
            }
            this.removedIdx[this.removedCount++] = idx;
        }
    }

    public static void sync(Vault vault, Version version, List<ServerPlayer> eligible, State state) {
        state.eligibleIds.clear();
        for (ServerPlayer player : eligible) {
            state.eligibleIds.add(player.getUUID());
        }
        state.synced.retainAll(state.eligibleIds);

        if (eligible.isEmpty()) {
            return;
        }

        boolean anyVeteran = false;
        for (ServerPlayer player : eligible) {
            if (state.synced.contains(player.getUUID())) {
                anyVeteran = true;
                break;
            }
        }

        try {
            boolean anyChange = collect(vault, version, eligible.get(0).getUUID(), state, anyVeteran);

            byte[] deltaBody = (anyVeteran && anyChange) ? writeDelta(state) : null;

            Packet<?> deltaPacket = null;
            Packet<?> fullPacket = null;
            for (ServerPlayer player : eligible) {
                if (state.synced.contains(player.getUUID())) {
                    if (deltaBody != null) {
                        if (deltaPacket == null) {
                            deltaPacket = packet(version, deltaBody);
                        }
                        player.connection.send(deltaPacket);
                    }
                } else {
                    if (fullPacket == null) {
                        fullPacket = packet(version, writeKeyframe(state));
                    }
                    player.connection.send(fullPacket);
                    state.synced.add(player.getUUID());
                }
            }
        } catch (Exception e) {
            state.invalidate();
            Unobtanium.LOGGER.error("[unobtanium] vault field-sync failed; will resend full", e);
        }
    }

    private static boolean collect(Vault vault, Version version, UUID sampleObserver, State state, boolean needDelta) {
        KeyRegistry fields = vault.getFields();
        ClientSyncContext ctx = new ClientSyncContext(version, sampleObserver);

        Arrays.fill(state.seen, 0, state.seen.length, false);
        state.changedCount = 0;
        state.removedCount = 0;
        state.deltaStage.clear();

        for (Object rawKey : fields.getKeys()) {
            GenericFieldKey key = (GenericFieldKey) rawKey;
            if (!vault.has(key)) {
                continue;
            }
            int idx = fields.getIndex(key.getId(), version);
            if (idx < 0) {
                continue;
            }
            Object value = vault.get(key);
            if (!key.canSync(value, ctx)) {
                continue;
            }

            state.scratch.reset();
            key.writeValue(version, state.scratch, ctx, value);
            int used = state.scratch.usedLongs();
            long[] cur = state.scratch.backing();

            state.ensureIndex(idx);
            state.seen[idx] = true;

            long[] base = state.baseline[idx];
            int baseLen = base == null ? -1 : state.baselineLen[idx];

            boolean changed = baseLen != used;
            if (!changed) {
                for (int i = 0; i < used; i++) {
                    if (cur[i] != base[i]) {
                        changed = true;
                        break;
                    }
                }
            }
            if (!changed) {
                continue;
            }

            if (needDelta) {
                state.deltaStage.writeVarInt(idx);
                LongArrayDelta.write(state.deltaStage, cur, used, base, base == null ? 0 : baseLen);
            }

            state.baseline[idx] = growInto(base, cur, used);
            state.baselineLen[idx] = used;
            state.changedCount++;
        }

        // fields present in the baseline but not seen this pass have been removed from the vault
        for (int i = 0; i < state.baseline.length; i++) {
            if (state.baseline[i] != null && !state.seen[i]) {
                state.addRemoved(i);
                state.baseline[i] = null;
            }
        }

        return state.changedCount > 0 || state.removedCount > 0;
    }

    // dynamically sizing the array
    private static long[] growInto(long[] dst, long[] src, int len) {
        if (dst == null || dst.length < len) {
            int cap = Math.max(len, dst == null ? 4 : dst.length * 2);
            dst = new long[cap];
        }
        System.arraycopy(src, 0, dst, 0, len);
        return dst;
    }

    private static byte[] writeDelta(State state) {
        FriendlyByteBuf buf = state.out;
        buf.clear();
        buf.writeBoolean(false); // delta, not a keyframe
        buf.writeVarInt(state.changedCount);
        state.deltaStage.readerIndex(0);
        buf.writeBytes(state.deltaStage, state.deltaStage.readableBytes());
        buf.writeVarInt(state.removedCount);
        for (int i = 0; i < state.removedCount; i++) {
            buf.writeVarInt(state.removedIdx[i]);
        }
        return toBytes(buf);
    }

    private static byte[] writeKeyframe(State state) {
        int count = 0;
        for (long[] field : state.baseline) {
            if (field != null) {
                count++;
            }
        }

        FriendlyByteBuf buf = state.out;
        buf.clear();
        buf.writeBoolean(true); // keyframe
        buf.writeVarInt(count);
        for (int i = 0; i < state.baseline.length; i++) {
            long[] field = state.baseline[i];
            if (field == null) {
                continue;
            }
            buf.writeVarInt(i);
            LongArrayDelta.write(buf, field, state.baselineLen[i], null, 0); // null base -> whole value
        }
        buf.writeVarInt(0);
        return toBytes(buf);
    }

    private static Packet<?> packet(Version version, byte[] body) {
        return UnobtaniumNetwork.CHANNEL.toVanillaPacket(new FieldSyncMessage(version, body), NetworkDirection.PLAY_TO_CLIENT);
    }

    private static byte[] toBytes(FriendlyByteBuf buf) {
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        return out;
    }
}
