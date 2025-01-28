//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import iskallia.vault.container.inventory.CardDeckContainer;
import iskallia.vault.core.card.Card;
import iskallia.vault.item.CardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;


/**
 * Vault Decks contained a lot of unnecessary information that are not used in any processes.
 * This mixin removes `inventory` that is leftover thing from u15 and places `null` in deck slots,
 * instead of having empty instances of Card objects.
 */
@Mixin(CardDeckContainer.class)
public class OptimizeCardDeckContainer
{
    @Shadow(remap = false)
    @Final
    private ItemStack delegate;

    @Redirect(method = "<init>",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;)Z"))
    private boolean removeInventoryTag(CompoundTag instance, String inventory)
    {
        // Remove inventory tag. It should be there regardless, as getSharedTag removes "inventory"
        // but just in case, remove it to avoid any possible issues with actual cards being added
        // at the end.
        instance.remove(inventory);
        return false;
    }


    @Redirect(method = "setChanged",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/item/CardItem;getCard(Lnet/minecraft/world/item/ItemStack;)Liskallia/vault/core/card/Card;"),
        remap = false)
    private Card ignoreAirItems(ItemStack stack)
    {
        // This will reduce memory consumption of decks, as will allow empty decks to be emtpy.
        return stack.isEmpty() ? null : CardItem.getCard(stack);
    }


    @Inject(method = "setChanged",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/item/CardDeckItem;setCardDeck(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/core/card/CardDeck;)V"),
        remap = false)
    private void removeInventoryTag(CallbackInfo ci)
    {
        // This tag is not used at all and is removed in other places. No need to save it.
        this.delegate.getOrCreateTag().remove("inventory");
    }
}
