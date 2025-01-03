package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(VaultChestTileEntity.class)
public abstract class FixesVaultBarrelNestingTileEntity implements Container
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