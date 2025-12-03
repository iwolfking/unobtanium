package xyz.iwolfking.unobtainium.mixin.transmog;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import xyz.iwolfking.unobtainium.fixes.TransmogRenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @WrapMethod(method="render")
    private <E extends Entity> void wrapRender(E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pMatrixStack,
                                              MultiBufferSource pBuffer, int pPackedLight, Operation<Void> original) {
        TransmogRenderUtils.enterInventoryExcludedClass();
        try {
            original.call(pEntity, pX, pY, pZ, pRotationYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        } finally {
            TransmogRenderUtils.exitInventoryExcludedClass();
        }
    }
}