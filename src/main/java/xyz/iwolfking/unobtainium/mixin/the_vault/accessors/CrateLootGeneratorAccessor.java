//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

import iskallia.vault.core.vault.CrateLootGenerator;
import net.minecraft.world.item.ItemStack;


@Mixin(value = CrateLootGenerator.class, remap = false)
public interface CrateLootGeneratorAccessor
{
    @Accessor("additionalItems")
    List<ItemStack> getAdditionalItems();
}
