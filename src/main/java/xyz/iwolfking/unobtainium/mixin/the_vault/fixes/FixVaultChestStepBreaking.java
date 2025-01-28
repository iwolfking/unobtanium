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


/**
 * U16 replaced `[type]_chest_placeable` with `[type]_chest`. However, it messed with chests that had step breaking,
 * as it did not check if chest is vault chest or player placed chest. It resulted in very bad behaviour.
 * This mixin fixes it, as it enables step breaking only if chest is vault chest.
 */
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
