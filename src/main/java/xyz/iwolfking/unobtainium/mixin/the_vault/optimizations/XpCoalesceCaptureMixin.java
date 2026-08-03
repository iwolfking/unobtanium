package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.drops.DropCoalescer;

@Mixin(Block.class)
public abstract class XpCoalesceCaptureMixin {

    @Inject(
        method = "popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void unobtainium$captureBurstXp(ServerLevel level, BlockPos pos, int amount, CallbackInfo ci) {
        if (DropCoalescer.addExperience(level, pos, amount)) {
            ci.cancel();
        }
    }
}
