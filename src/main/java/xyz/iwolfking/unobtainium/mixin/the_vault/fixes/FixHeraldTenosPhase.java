package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.entity.boss.ConjurationMagicProjectileEntity;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.iwolfking.unobtainium.mixin.the_vault.accessors.VaultChestTileEntityAccessor;


/**
 * U16 replaced `[type]_chest_placeable` with `[type]_chest`. The indication if chest should be targeted as Vault Chest
 * now only relied on `VaultChestTileEntity#vaultChest` variable value.
 * This value was not set to the chests generated in Tenos phase, which resulted in empty chests.
 * This mixin sets that given chest is Vault Chest so it could generate loot inside it.
 */
@Mixin(value = ConjurationMagicProjectileEntity.class, remap = false)
public class FixHeraldTenosPhase {

    @Inject(method = "lambda$placeChest$0", at = @At("TAIL"))
    private void injectChestType(Block chestBlock, VaultChestTileEntity chest, CallbackInfo ci)
    {
        ((VaultChestTileEntityAccessor) chest).setVaultChest(true);
        chest.setChanged();
    }
}
