package xyz.iwolfking.unobtainium.mixin.transmog;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import xyz.iwolfking.unobtainium.fixes.TransmogRenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @WrapMethod(method="render")
    private void wrapRender(float pPartialTicks, long pNanoTime, boolean pRenderLevel, Operation<Void> original) {
        TransmogRenderUtils.enterRenderClass();
        try {
            original.call(pPartialTicks, pNanoTime, pRenderLevel);
        } finally {
            TransmogRenderUtils.exitRenderClass();
        }
    }

    @WrapOperation(method = "render", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void wrapGuiRender(Gui instance, PoseStack i, float f3, Operation<Void> original) {
        TransmogRenderUtils.enterInventoryClass();
        try {
            original.call(instance, i, f3);
        } finally {
            TransmogRenderUtils.exitInventoryClass();
        }
    }

    @WrapMethod(method="tick")
    private void wrapTick(Operation<Void> original) {
        TransmogRenderUtils.enterRenderClass();
        try {
            original.call();
        } finally {
            TransmogRenderUtils.exitRenderClass();
        }
    }
}