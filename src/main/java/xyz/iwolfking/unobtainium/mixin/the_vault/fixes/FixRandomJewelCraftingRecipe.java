//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import iskallia.vault.VaultMod;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.gear.crafting.recipe.JewelCraftingRecipe;
import iskallia.vault.item.gear.DataInitializationItem;
import iskallia.vault.item.gear.DataTransferItem;
import iskallia.vault.item.gear.VaultLevelItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


/**
 * U16 removed ability to craft random jewels, as it was expecting for players to only find jewels.
 * This change fixes it, by generating random jewel if `jewelAttribute` in `jewel_crafting_recipes.json`
 * is specified as `the_vault:empty`.
 * The recipe still requires loot table to be specified for jewels (like pouches)
 */
@Mixin(value = JewelCraftingRecipe.class, remap = false)
public abstract class FixRandomJewelCraftingRecipe
{
    @Shadow
    private ResourceLocation jewelAttribute;

    @Inject(method = "createOutput", at = @At(value = "RETURN"), cancellable = true)
    private void allowRandomJewelCrafting(List<OverSizedItemStack> consumed,
        ServerPlayer crafter,
        int vaultLevel,
        CallbackInfoReturnable<ItemStack> cir)
    {
        // If attribute is empty, use random gear roller to get a jewel based on its loot table.
        if (this.jewelAttribute.equals(VaultMod.id("random")))
        {
            ItemStack stack = cir.getReturnValue();
            VaultLevelItem.doInitializeVaultLoot(stack, vaultLevel);
            stack = DataTransferItem.doConvertStack(stack, JavaRandom.ofNanoTime());
            DataInitializationItem.doInitialize(stack, JavaRandom.ofNanoTime());
            cir.setReturnValue(stack);
        }
    }
}
