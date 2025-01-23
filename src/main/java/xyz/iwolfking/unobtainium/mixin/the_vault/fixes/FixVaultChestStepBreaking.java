//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(VaultChestBlock.class)
public class FixVaultChestStepBreaking
{
    @Redirect(method = "onDestroyedByPlayer",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/block/VaultChestBlock;hasStepBreaking()Z"),
        remap = false)
    private boolean fixOnDestroyedByPlayer(VaultChestBlock instance, @Local BlockEntity te)
    {
        // Only Vault Chests (with loot data) should have step breaking
        return instance.hasStepBreaking() && te instanceof VaultChestTileEntity chest && chest.isVaultChest();
    }


    @Redirect(method = "playerDestroy",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/block/VaultChestBlock;hasStepBreaking()Z", remap = false)
        )
    private boolean fixPlayerDestroy(VaultChestBlock instance, @Local(argsOnly = true) BlockEntity te)
    {
        // Only Vault Chests (with loot data) should have step breaking
        return instance.hasStepBreaking() && te instanceof VaultChestTileEntity chest && chest.isVaultChest();
    }
}
