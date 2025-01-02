//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.world.Container;


@Mixin(VaultChestTileEntity.class)
public class FixVaultChestGeneratedLootAmount
{
    @Redirect(method = "fillLoot", at = @At(value = "FIELD", target = "Liskallia/vault/block/entity/VaultChestTileEntity;size:I", opcode = Opcodes.GETFIELD))
    public int fixGeneratedLootSlots(VaultChestTileEntity instance)
    {
        // All generated vault chest inventories should be 27 items inside it.
        return 27;
    }


    @Redirect(method = "getAvailableSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I"))
    public int fixFilledSlots(Container instance)
    {
        // All generated vault chest inventories should be 27 items inside it.
        return 27;
    }
}
