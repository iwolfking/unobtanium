//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.item.JewelPouchItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


@Mixin(JewelPouchItem.class)
public class FixJewelIdentificationOverwrite
{
    @Inject(method = "use",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/item/JewelPouchItem;getJewels(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            ordinal = 0))
    private void dropIdentifiedJewels(Level level,
        Player player,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
        @Local ItemStack stack)
    {
        if (stack.getCount() > 1)
        {
            // make copy and drop all remining on ground.
            ItemStack droppingItems = stack.copy();
            droppingItems.shrink(1);
            player.drop(droppingItems, false, false);
            // Keep only 1 item in stack.
            stack.setCount(1);
        }
    }
}
