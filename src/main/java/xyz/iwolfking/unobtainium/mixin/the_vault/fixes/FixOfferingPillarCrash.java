package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.entity.OfferingPillarTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.unobtainium.api.helper.OfferingPillarHelper;

@Mixin(value = OfferingPillarTileEntity.class, remap = false)
public abstract class FixOfferingPillarCrash extends BlockEntity {

    public FixOfferingPillarCrash(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    /**
     * @author iwolfking
     * @reason Why we trying to run ClientLevel on server, folks?
     */
    @Overwrite()
    public static void tick(Level world, BlockPos pos, BlockState state, OfferingPillarTileEntity entity) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> {
            if(world.isClientSide()) {
                return () -> OfferingPillarHelper.safelyCallClientTick(entity, world, pos, state);
            }
            else {
                return () -> {
                    if (world instanceof ServerLevel serverWorld) {
                        entity.tickServer(serverWorld, pos, state);
                    }
                };
            }
        });

        if(!world.isClientSide()) {
            if (world instanceof ServerLevel serverWorld) {
                entity.tickServer(serverWorld, pos, state);
            }
        }
    }
}