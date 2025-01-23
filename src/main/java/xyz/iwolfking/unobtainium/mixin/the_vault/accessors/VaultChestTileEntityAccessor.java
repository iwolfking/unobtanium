package xyz.iwolfking.unobtainium.mixin.the_vault.accessors;

import iskallia.vault.block.entity.VaultChestTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VaultChestTileEntity.class, remap = false)
public interface VaultChestTileEntityAccessor {
    @Accessor("vaultChest")
    void setVaultChest(boolean value);
}
