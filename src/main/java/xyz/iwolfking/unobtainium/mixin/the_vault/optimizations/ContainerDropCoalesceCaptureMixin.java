package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.drops.DropCoalescer;

//capture spilled contents from broken containers
@Mixin(Containers.class)
public abstract class ContainerDropCoalesceCaptureMixin {

    @Inject(
        method = "dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unobtainium$captureContainerDrop(Level level, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (DropCoalescer.capture(level, new BlockPos(x, y, z), stack)) {
            ci.cancel();
        }
    }
}
