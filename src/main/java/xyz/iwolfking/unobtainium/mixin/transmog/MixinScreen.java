package xyz.iwolfking.unobtainium.mixin.transmog;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import xyz.iwolfking.unobtainium.fixes.TransmogRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(Screen.class)
public class MixinScreen {

    @WrapMethod(method = "render")
    private void wrapRender(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, Operation<Void> original){
        TransmogRenderUtils.enterInventoryClass();
        try {
            original.call(pPoseStack, pMouseX, pMouseY, pPartialTick);
        } finally {
            TransmogRenderUtils.exitInventoryClass();
        }
    }

    @WrapMethod(method = "renderTooltipInternal")
    private void wrapRenderTooltip(PoseStack pPoseStack, List<ClientTooltipComponent> pClientTooltipComponents, int pMouseX, int pMouseY, Operation<Void> original){
        TransmogRenderUtils.enterInventoryClass();
        try {
            original.call(pPoseStack,pClientTooltipComponents, pMouseX, pMouseY);
        } finally {
            TransmogRenderUtils.exitInventoryClass();
        }
    }
}
