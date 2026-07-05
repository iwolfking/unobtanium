package xyz.iwolfking.unobtainium.sync;

import io.netty.buffer.Unpooled;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.GenericFieldKey;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.data.sync.context.ClientSyncContext;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.core.vault.Vault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import xyz.iwolfking.unobtainium.Unobtanium;

public final class ServerFieldSync {

    private ServerFieldSync() {
    }

    public static final class State {
        final Map<Integer, long[]> baseline = new HashMap<>();
        final Set<UUID> synced = new HashSet<>();

        void invalidate() {
            this.baseline.clear();
            this.synced.clear();
        }
    }

    private static final class FieldSnapshot {
        final int index;
        final long[] bytes;

        FieldSnapshot(int index, long[] bytes) {
            this.index = index;
            this.bytes = bytes;
        }
    }

    public static void sync(Vault vault, Version version, List<ServerPlayer> eligible, State state) {
        Set<UUID> eligibleIds = new HashSet<>();
        for (ServerPlayer player : eligible) {
            eligibleIds.add(player.getUUID());
        }
        state.synced.retainAll(eligibleIds);

        if (eligible.isEmpty()) {
            return;
        }

        try {
            List<FieldSnapshot> snapshot = serialize(vault, version, eligible.get(0).getUUID());

            boolean anyVeteran = false;
            for (ServerPlayer player : eligible) {
                if (state.synced.contains(player.getUUID())) {
                    anyVeteran = true;
                    break;
                }
            }

            byte[] deltaBody = anyVeteran ? diff(snapshot, state.baseline) : null;
            if (!anyVeteran) {
                rebaseline(snapshot, state.baseline);
            }

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
                        fullPacket = packet(version, keyframe(snapshot));
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

    private static List<FieldSnapshot> serialize(Vault vault, Version version, UUID sampleObserver) {
        KeyRegistry fields = vault.getFields();
        ClientSyncContext ctx = new ClientSyncContext(version, sampleObserver);
        List<FieldSnapshot> snapshot = new ArrayList<>();

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
            ArrayBitBuffer fieldBuf = ArrayBitBuffer.empty();
            key.writeValue(version, fieldBuf, ctx, value);
            snapshot.add(new FieldSnapshot(idx, fieldBuf.toLongArray()));
        }
        return snapshot;
    }

    private static byte[] diff(List<FieldSnapshot> snapshot, Map<Integer, long[]> baseline) {
        List<FieldSnapshot> changed = new ArrayList<>();
        List<long[]> changedBase = new ArrayList<>();
        Set<Integer> currentIdx = new HashSet<>();

        for (FieldSnapshot fs : snapshot) {
            currentIdx.add(fs.index);
            long[] cached = baseline.get(fs.index);
            if (cached != null && Arrays.equals(cached, fs.bytes)) {
                continue;
            }
            changed.add(fs);
            changedBase.add(cached);
            baseline.put(fs.index, fs.bytes);
        }

        List<Integer> removed = new ArrayList<>();
        for (Integer idx : new ArrayList<>(baseline.keySet())) {
            if (!currentIdx.contains(idx)) {
                removed.add(idx);
                baseline.remove(idx);
            }
        }

        if (changed.isEmpty() && removed.isEmpty()) {
            return null;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(false); // delta, not a keyframe
        buf.writeVarInt(changed.size());
        for (int i = 0; i < changed.size(); i++) {
            buf.writeVarInt(changed.get(i).index);
            LongArrayDelta.write(buf, changed.get(i).bytes, changedBase.get(i));
        }
        buf.writeVarInt(removed.size());
        for (int idx : removed) {
            buf.writeVarInt(idx);
        }
        return toBytes(buf);
    }

    private static byte[] keyframe(List<FieldSnapshot> snapshot) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true); // keyframe -> client resets its cache first
        buf.writeVarInt(snapshot.size());
        for (FieldSnapshot fs : snapshot) {
            buf.writeVarInt(fs.index);
            LongArrayDelta.write(buf, fs.bytes, null); // null base -> whole value
        }
        buf.writeVarInt(0);
        return toBytes(buf);
    }

    private static void rebaseline(List<FieldSnapshot> snapshot, Map<Integer, long[]> baseline) {
        baseline.clear();
        for (FieldSnapshot fs : snapshot) {
            baseline.put(fs.index, fs.bytes);
        }
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
