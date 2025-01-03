//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.block.VaultBarrelBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;


@Mixin(BlockItem.class)
public abstract class FixVaultBarrelNesting
{
    @Final
    @Shadow
    private Block block;


    @Inject(method = "canFitInsideContainerItems", at = @At("RETURN"), cancellable = true)
    private void injectVaultBarrelBlock(@NotNull CallbackInfoReturnable<Boolean> cir)
    {
        // Vault barrel should be considered as shulker box
        cir.setReturnValue(cir.getReturnValue() && !(this.block instanceof VaultBarrelBlock));
    }
}
