//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import iskallia.vault.container.inventory.CardDeckContainer;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.item.CardItem;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;


@Mixin(CardDeckContainer.class)
public abstract class FixCardDeckSyncIssues extends SimpleContainer
{
    @Shadow(remap = false)
    @Final
    private CardDeck deck;


    @Shadow(remap = false)
    @Final
    private Map<Integer, CardPos> slotMapping;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void populateDeckCards(ItemStack delegate, CallbackInfo ci)
    {
        // They "optimize" decks to not store tags in inventory tag, but only as Card in data section.
        // However, it was never used to populate card deck container, which caused all issues
        // with sync and card disappearing.

        for (CardPos pos : this.deck.getSlots())
        {
            int slot = pos.x + pos.y * 9;
            this.slotMapping.put(slot, pos);

            this.deck.getCard(pos).ifPresent(card ->
            {
                // Also funny but deck contains all cards always. That is due to the fact that
                // CardItem#getCard that is used for updating deck container on setChanged always
                // return Card instance, even if it is emtpy.
                if (!card.getEntries().isEmpty())
                {
                    ItemStack cardItem = CardItem.create(card);
                    this.setItem(slot, cardItem);
                }
            });
        }
    }
}
