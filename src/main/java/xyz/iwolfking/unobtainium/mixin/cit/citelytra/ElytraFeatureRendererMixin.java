package xyz.iwolfking.unobtainium.mixin.cit.citelytra;

import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;

import java.lang.ref.WeakReference;
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "citresewn")
    }
)
@Mixin({ElytraLayer.class})
public class ElytraFeatureRendererMixin {
    private WeakReference<ItemStack> elytraItemCached = null;
    private WeakReference<LivingEntity> livingEntityCached = null;

    @Inject(
        method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
        at = {@At("HEAD")}
    )
    private void render(
        PoseStack matrixStack,
        MultiBufferSource vertexConsumerProvider,
        int i,
        LivingEntity livingEntity,
        float f,
        float g,
        float h,
        float j,
        float k,
        float l,
        CallbackInfo ci
    ) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
            if (itemStack.isEmpty() || !itemStack.is(Items.ELYTRA)) {
                return;
            }
            this.elytraItemCached = new WeakReference<>(itemStack);
            this.livingEntityCached = new WeakReference<>(livingEntity);
        }
    }

    @ModifyArg(
        method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
        ),
        index = 1
    )
    private RenderType getArmorCutoutNoCull(RenderType original) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            var cachedElytra = this.elytraItemCached;
            var cachedLivingEntity = this.livingEntityCached;
            if (cachedElytra == null || cachedLivingEntity == null) {
                return original;
            }
            ItemStack itemStack = cachedElytra.get();
            LivingEntity livingEntity = cachedLivingEntity.get();
            if (itemStack != null && itemStack.is(Items.ELYTRA) && livingEntity != null) {
                ResourceLocation elytraTexture = CITResewn.INSTANCE.activeCITs.getElytraTextureCached(itemStack, livingEntity.level, livingEntity);
                this.elytraItemCached = null;
                this.livingEntityCached = null;
                if (elytraTexture != null) {
                    return RenderType.armorCutoutNoCull(elytraTexture);
                }
            }
        }

        return original;
    }
}
