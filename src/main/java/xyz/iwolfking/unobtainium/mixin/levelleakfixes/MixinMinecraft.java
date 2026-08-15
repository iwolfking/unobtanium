package xyz.iwolfking.unobtainium.mixin.levelleakfixes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.fixes.ServerStoppingMemoryLeakFix;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "updateLevelInEngines", at = @At("TAIL"))
    private void fixPNCLeak(ClientLevel pLevel, CallbackInfo ci){
        ServerStoppingMemoryLeakFix.clearPNCClientHandlers();
    }
}
