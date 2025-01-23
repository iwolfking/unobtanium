package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.block.TotemBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.WorldZone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/*
    This code is a replacement for the mixin in Vault Hunters that prevents blockstates from changing in rooms like Raid.
    The original prevents ALL blockstates from changing, but this includes things like totems that need to de-spawn. This fixes that issue.
*/
@Mixin(value = LevelChunk.class, priority = 1500)
public abstract class FixTotemsInRaidRooms {
    @Shadow
    public abstract Level getLevel();

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> ci) {
        if (!this.getLevel().isClientSide()) {
            if(getLevel().getBlockState(pos).getBlock() instanceof TotemBlock || getLevel().getBlockState(pos).getBlock() instanceof VaultChestBlock || getLevel().getBlockState(pos).getBlock() instanceof CoinPileBlock) {
                return;
            }
            IZonedWorld proxy = IZonedWorld.of(this.getLevel()).orElse(null);
            if (proxy != null) {
                List<WorldZone> zones = proxy.getZones().get(pos);
                if (!zones.isEmpty()) {
                    for (WorldZone zone : zones) {
                        if (zone.canModify() == Boolean.FALSE) {
                            ci.setReturnValue(null);
                            return;
                        }
                    }
                }
            }
        }
    }
}
