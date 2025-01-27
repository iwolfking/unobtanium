//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import net.minecraft.nbt.CompoundTag;


@Mixin(value = CardDeck.class, remap = false)
public class OptimizeCardDecks
{
    @Redirect(method = "readNbt(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object fixNullNbtReading(Map instance, Object k, Object v, @Local(index = 4) CompoundTag entry)
    {
        // for some reason the adapters were flipped. Position cannot be null, but cards can. 
        return instance.put(CardPos.ADAPTER.readNbt(entry.get("pos")).orElseThrow(),
            entry.contains("card") ? Card.ADAPTER.readNbt(entry.getCompound("card")).orElse(null) : null);
    }
}
