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


@Mixin(value = VaultChestRenderer.class, remap = false)
public class FixVaultBarrelDisplayItemSize
{
    @Redirect(method = "renderFirstItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void changeDisplayItemSize(PoseStack instance, float p_85842_, float p_85843_, float p_85844_)
    {
        // 0.3 value is very small. Make it 0.5 for easier viewing.
        instance.scale(0.5f, 0.5f, 0.5f);
    }
}
