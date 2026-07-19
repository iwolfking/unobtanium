package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.drops.DropCoalescer;

@Mixin(Block.class)
public abstract class DropCoalesceCaptureMixin {

    @Inject(
        method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unobtainium$captureBurstDrop(Level level, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (DropCoalescer.capture(level, pos, stack)) {
            ci.cancel();
        }
    }
}
