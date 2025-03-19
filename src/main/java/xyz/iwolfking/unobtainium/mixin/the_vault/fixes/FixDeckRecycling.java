package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.entity.CardEssenceExtractorTileEntity;
import iskallia.vault.config.CardEssenceExtractorConfig;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.item.CardItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
    The developers added extracting essence from decks, but it doesn't bother checking if the deck contains any cards. This prevents extracting essence from a deck containing cards.
*/
@Mixin(value = CardEssenceExtractorTileEntity.class, remap = false)
public abstract class FixDeckRecycling {
    @Shadow public abstract ItemStack getEssenceInputStack();

    @Shadow private int extractWorkTick;
    @Shadow private int maxExtractWorkTick;

    @Shadow public abstract void sendUpdates();

    @Redirect(method = "startExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", remap = true))
    public boolean startExtract(ItemStack instance) {
        if(instance.getItem() instanceof CardDeckItem) {
            CardDeck deck = CardDeckItem.getCardDeck(instance).orElse(null);
            if(deck != null && !deck.getSnapshotAttributes().isEmpty()) {
                return true;
            }
        }

        return instance.isEmpty();

    }
}
