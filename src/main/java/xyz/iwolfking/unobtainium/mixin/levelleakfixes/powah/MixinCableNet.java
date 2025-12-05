package xyz.iwolfking.unobtainium.mixin.levelleakfixes.powah;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owmii.powah.block.cable.CableTile;

@Mixin(targets = "owmii.powah.block.cable.CableNet", remap = false)
public class MixinCableNet {
    @Inject(method = {"addCable", "removeCable", "updateAdjacentCables", "calculateNetwork"}, at = @At("HEAD"), cancellable = true)
    private static void returnIfNotServer(CableTile cable, CallbackInfo ci){
        if (!(cable.getLevel() instanceof ServerLevel)) {
            ci.cancel();
        }
    }
}
