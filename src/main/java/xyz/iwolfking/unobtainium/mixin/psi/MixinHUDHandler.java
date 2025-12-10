package xyz.iwolfking.unobtainium.mixin.psi;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.core.handler.HUDHandler;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "psi")
    }
)
@Mixin(value = HUDHandler.class, remap = false)
public class MixinHUDHandler {
    @Unique private static boolean unobtainium$shouldRender = false;
    @Unique private static int unobtainium$lastTick = 0;

    @Inject(method = "drawPsiBar", at = @At(value = "HEAD"), cancellable = true)
    private static void cancelRender(PoseStack ms, Window res, float pticks, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.tickCount != unobtainium$lastTick) {
                unobtainium$shouldRender = false;
                unobtainium$lastTick = player.tickCount;
            } else if (!unobtainium$shouldRender) {
                ci.cancel();
            }
        }
    }

    // first rendering instruction after passing all checks
    @Inject(method = "drawPsiBar", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, remap = true))
    private static void rendered(PoseStack ms, Window res, float pticks, CallbackInfo ci) {
        unobtainium$shouldRender = true;
    }
}
