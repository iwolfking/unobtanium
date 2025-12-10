//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.init.ModItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.injection.Slice;


/**
 * This was a feature request by HoY, to allow stack more jewel pouches in inventory. Having only one item
 * per slot felt very limiting.
 */
@Mixin(value = ModItems.class)
public class FixJewelPouchStackSize
{
    @Redirect(method = "<clinit>",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item$Properties;stacksTo(I)Lnet/minecraft/world/item/Item$Properties;",
            ordinal = 0),
        slice = @Slice(
            from = @At(value = "NEW", target = "(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/Item$Properties;)Liskallia/vault/item/JewelPouchItem;", remap = false),
            to = @At(value = "NEW", target = "(Lnet/minecraft/resources/ResourceLocation;)Liskallia/vault/item/gear/VaultCharmItem;", remap = false)))
    private static Item.Properties redirectProperties(Item.Properties instance, int pMaxStackSize)
    {
        // 16 would be good, as having 1 per stack makes pouches useful only for backpacks.
        return instance.stacksTo(16);
    }
}
