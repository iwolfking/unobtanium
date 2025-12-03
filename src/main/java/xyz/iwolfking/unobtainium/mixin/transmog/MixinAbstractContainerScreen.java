package xyz.iwolfking.unobtainium.mixin.transmog;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.unobtainium.fixes.TransmogRenderUtils;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @WrapMethod(method = "render")
    private void wrapRender(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, Operation<Void> original){
        TransmogRenderUtils.enterInventoryClass();
        try {
            original.call(pPoseStack, pMouseX, pMouseY, pPartialTick);
        } finally {
            TransmogRenderUtils.exitInventoryClass();
        }
    }

}
