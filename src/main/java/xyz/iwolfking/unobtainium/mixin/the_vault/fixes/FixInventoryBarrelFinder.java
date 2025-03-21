//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


/**
 * U16 introduced Vault Barrels that acts like shulker boxes (keeps inventory even when are picked up).
 * However, InventoryUtil ignored barrels, so inventory inside them were not targeted by rotting and
 * identifications (and other processes that used InventoryUtil for item searching).
 *
 * This mixin injects Vault Barrel processing in `shulker box` finder as they act very similar.
 */
@Mixin(value = InventoryUtil.class, remap = false)
public class FixInventoryBarrelFinder
{
    @Inject(method = "isShulkerBox", at = @At("HEAD"), cancellable = true)
    private static void injectVaultBarrelCheck(Item item, CallbackInfoReturnable<Boolean> cir)
    {
        // Shulker boxes and barrels has same data storage way, so it should be ok to pretend it is shulker.
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultBarrelBlock)
        {
            cir.setReturnValue(true);
        }
    }


    @Redirect(method = "getShulkerBoxAccess",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"), remap = true)
    private static NonNullList<ItemStack> injectBarrelSize(int size, Object value, @Local ItemStack container)
    {
        int returnSize = size;

        // Adjust the size of vault barrels.
        if (container.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultBarrelBlock)
        {
            if (container.is(ModBlocks.WOODEN_BARREL.asItem()))
            {
                returnSize = 36;
            }
            else
            {
                returnSize = 45;
            }
        }

        return NonNullList.withSize(returnSize, ItemStack.EMPTY);
    }


    @Redirect(method = "lambda$getShulkerBoxAccess$17",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;",remap = true))
    private static NonNullList<ItemStack> injectBarrelSizeAgain(int size, Object value, @Local(argsOnly = true, ordinal = 0) ItemStack container)
    {
        int returnSize = size;

        // Adjust the size of vault barrels.
        if (container.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultBarrelBlock)
        {
            if (container.is(ModBlocks.WOODEN_BARREL.asItem()))
            {
                returnSize = 36;
            }
            else
            {
                returnSize = 45;
            }
        }

        return NonNullList.withSize(returnSize, ItemStack.EMPTY);
    }
}
