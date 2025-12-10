package xyz.iwolfking.unobtainium.fixes;

import net.minecraft.core.BlockPos;

public class VaultPosReuse {

    private static final BlockPos[] positions = new BlockPos[47 * 47 * 47];
    public static BlockPos sharedPos(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        int idx = pos.getX() + pos.getY() * 47 + pos.getZ() * 2209;
        if (idx < 0 || idx >= positions.length) {
            return pos;
        }
        BlockPos shared = positions[idx];
        if (shared == null) {
            positions[idx] = pos.immutable(); // immutable for safety (no-op if it was already immutable)
            return positions[idx];
        }
        return shared;
    }
}
