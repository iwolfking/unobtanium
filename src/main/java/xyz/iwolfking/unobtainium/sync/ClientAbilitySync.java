package xyz.iwolfking.unobtainium.sync;

import io.netty.buffer.Unpooled;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import xyz.iwolfking.unobtainium.Unobtanium;
import xyz.iwolfking.unobtainium.mixin.the_vault.accessors.AbilityTreeAccessor;

public final class ClientAbilitySync {

    private static long[][] nodeCache;

    private ClientAbilitySync() {
    }

    public static void invalidateCache() {
        nodeCache = null;
    }

    public static void apply(byte[] body) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(body));
        int kind = buf.readByte();
        AbilityTree tree = ClientAbilityData.getTree();

        if (kind == ServerAbilitySync.KIND_FULL) {
            applyFull(tree, buf, body.length);
        } else {
            applyNodes(tree, buf, body.length);
        }
    }

    private static void applyFull(AbilityTree tree, FriendlyByteBuf buf, int wireBytes) {
        long[] full = buf.readLongArray();
        tree.readBits(ArrayBitBuffer.backing(full, 0));

        List<Skill> skills = tree.skills;
        long[][] cache = new long[skills.size()][];
        long fullBytes = 0L;
        for (int i = 0; i < skills.size(); i++) {
            ArrayBitBuffer nodeBuf = ArrayBitBuffer.empty();
            skills.get(i).writeBits(nodeBuf);
            cache[i] = nodeBuf.toLongArray();
            fullBytes += (long) cache[i].length * 8L;
        }
        nodeCache = cache;
    }

    private static void applyNodes(AbilityTree tree, FriendlyByteBuf buf, int wireBytes) {
        if (nodeCache == null) {
            Unobtanium.LOGGER.warn("[unobtanium] ability NODES with no prior FULL; skipping (a FULL will follow)");
            return;
        }

        boolean selectedChanged = buf.readBoolean();
        String selectedId = null;
        if (selectedChanged) {
            selectedId = buf.readBoolean() ? buf.readUtf() : null;
        }

        List<Skill> skills = tree.skills;
        int count = buf.readVarInt();
        for (int c = 0; c < count; c++) {
            int idx = buf.readVarInt();
            long[] node = LongArrayDelta.read(buf, nodeCache[idx]);
            nodeCache[idx] = node;
            // read into the existing node object (identity preserved -> 'selected' reference stays valid).
            skills.get(idx).readBits(ArrayBitBuffer.backing(node, 0));
        }

        if (selectedChanged) {
            // resolve the id the same way AbilityTree.readBits does; nodes were patched in place so getForId
            // returns the live object.
            AbilityTreeAccessor accessor = (AbilityTreeAccessor) tree;
            if (selectedId == null) {
                accessor.unobtainium$setSelected(null);
            } else {
                tree.getForId(selectedId)
                    .filter(s -> s instanceof SpecializedSkill)
                    .ifPresent(s -> accessor.unobtainium$setSelected((SpecializedSkill) s));
            }
        }
    }
}
