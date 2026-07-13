package xyz.iwolfking.unobtainium.sync;

import io.netty.buffer.Unpooled;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public final class ServerAbilitySync {

    static final int KIND_FULL = 0;
    static final int KIND_NODES = 1;
    // resend full every 5 mins just in-case?
    private static final int ANCHOR_INTERVAL = 6000;

    private ServerAbilitySync() {
    }

    public static final class State {
        long[][] nodes;
        String selectedId;
        int sinceAnchor;
        private WeakReference<Object> client;

        public void invalidate() {
            this.nodes = null;
        }

        boolean isNewClient(Object connection) {
            Object last = this.client == null ? null : this.client.get();
            return last != connection;
        }
    }

    public static byte[] buildPayload(AbilityTree tree, State state, Object connection) {
        List<Skill> skills = tree.skills;
        int n = skills.size();

        long[][] current = new long[n][];
        for (int i = 0; i < n; i++) {
            ArrayBitBuffer nodeBuf = ArrayBitBuffer.empty();
            skills.get(i).writeBits(nodeBuf);
            current[i] = nodeBuf.toLongArray();
        }
        SpecializedSkill selected = tree.getSelected();
        String selectedId = selected == null ? null : selected.getId();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        boolean forceFull = state.nodes == null
            || state.nodes.length != n
            || state.sinceAnchor >= ANCHOR_INTERVAL
            || state.isNewClient(connection);
        if (forceFull) {
            buf.writeByte(KIND_FULL);
            ArrayBitBuffer treeBuf = ArrayBitBuffer.empty();
            tree.writeBits(treeBuf);
            buf.writeLongArray(treeBuf.toLongArray());
            state.nodes = current;
            state.selectedId = selectedId;
            state.sinceAnchor = 0;
            state.client = new WeakReference<>(connection);
            return toBytes(buf);
        }

        List<Integer> changed = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!Arrays.equals(current[i], state.nodes[i])) {
                changed.add(i);
            }
        }
        boolean selectedChanged = !Objects.equals(selectedId, state.selectedId);
        if (changed.isEmpty() && !selectedChanged) {
            return null; // client already has this exact state
        }

        buf.writeByte(KIND_NODES);
        buf.writeBoolean(selectedChanged);
        if (selectedChanged) {
            buf.writeBoolean(selectedId != null);
            if (selectedId != null) {
                buf.writeUtf(selectedId);
            }
            state.selectedId = selectedId;
        }
        buf.writeVarInt(changed.size());
        for (int idx : changed) {
            buf.writeVarInt(idx);
            LongArrayDelta.write(buf, current[idx], state.nodes[idx]);
            state.nodes[idx] = current[idx];
        }
        state.sinceAnchor++;
        return toBytes(buf);
    }

    private static byte[] toBytes(FriendlyByteBuf buf) {
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        return out;
    }
}
