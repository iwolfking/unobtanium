package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Vault Barrels introduced in u16 did not have any restrictions what can be placed inside them.
 * This mixin changes it, as it forces vault barrel slots act like shulker boxes slots which prevents
 * any items that contains items be placed inside them.
 */
@Mixin(VaultChestTileEntity.class)
public abstract class FixVaultBarrelNestingTileEntity implements Container
{
    @Shadow
    public abstract BlockState getBlockState();

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        // Barrels should not allow other barrels or shulker boxes inside them.

        return !(this.getBlockState().getBlock() instanceof VaultBarrelBlock) ||
            stack.getItem().canFitInsideContainerItems();
    }
}