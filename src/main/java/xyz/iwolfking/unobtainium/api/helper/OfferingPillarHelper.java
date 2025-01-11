package xyz.iwolfking.unobtainium.api.helper;

import iskallia.vault.block.entity.OfferingPillarTileEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OfferingPillarHelper {

    public static void safelyCallClientTick(OfferingPillarTileEntity entity, Level world, BlockPos pos, BlockState state) {
        if(world instanceof ClientLevel level) {
            entity.tickClient(level, pos, state);
        }
    }
}
