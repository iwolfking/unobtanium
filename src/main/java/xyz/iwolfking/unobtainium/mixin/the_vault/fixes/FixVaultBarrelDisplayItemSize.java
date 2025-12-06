//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.block.render.VaultChestRenderer;


/**
 * Vault Barrel Items on display were set to be 0.3 in size. It felt too small, so this change makes it 0.5
 */
@Mixin(value = VaultChestRenderer.class)
public class FixVaultBarrelDisplayItemSize
{
    @Redirect(method = "renderFirstItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void changeDisplayItemSize(PoseStack instance, float pX, float pY, float pZ)
    {
        // 0.3 value is very small. Make it 0.5 for easier viewing.
        instance.scale(0.5f, 0.5f, 0.5f);
    }
}
