package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.unobtainium.menus.VaultBarrelMenu;


/**
 * U16 replaced `[type]_chest_placeable` with `[type]_chest`. However, the menu size were not updated
 * according to these changes, and all chests now had 27 slots, instead of their actual size.
 * This mixin fixes it by creating menu with correct slot count (based on VaultChestTileEntity#getSize)
 */
@Mixin(VaultChestBlock.class)
public class FixVaultChestMenuSize
{
    @Inject(method = "getMenuProvider", at = @At(value = "HEAD"), cancellable = true)
    public void fixChestDisplaySize(BlockState state,
        Level level,
        BlockPos pos,
        CallbackInfoReturnable<MenuProvider> cir)
    {
        // If chest is not Vault Chest, then it should open menu based on chest size.
        // Otherwise it will open 27.

        if (level.getBlockEntity(pos) instanceof VaultChestTileEntity chestEntity && !chestEntity.isVaultChest())
        {
            MenuProvider provider = new MenuProvider()
            {
                @Override
                @NotNull
                public Component getDisplayName()
                {
                    return chestEntity.getDisplayName();
                }


                @Override
                @Nullable
                public AbstractContainerMenu createMenu(int containerId,
                    @NotNull Inventory playerInventory,
                    @NotNull Player player)
                {
                    if (chestEntity.canOpen(player))
                    {
                        MenuType<ChestMenu> menuType;
                        int rows;

                        switch (chestEntity.getContainerSize())
                        {
                            case 36 ->
                            {
                                menuType = MenuType.GENERIC_9x4;
                                rows = 4;
                            }
                            case 45 ->
                            {
                                menuType = MenuType.GENERIC_9x5;
                                rows = 5;
                            }
                            case 54 ->
                            {
                                menuType = MenuType.GENERIC_9x6;
                                rows = 6;
                            }
                            default ->
                            {
                                menuType = MenuType.GENERIC_9x3;
                                rows = 3;
                            }
                        }

                        if (state.getBlock() instanceof VaultBarrelBlock)
                        {
                            return new VaultBarrelMenu(menuType,
                                containerId,
                                playerInventory,
                                chestEntity,
                                rows);
                        }
                        else
                        {
                            return new ChestMenu(menuType,
                                containerId,
                                playerInventory,
                                chestEntity,
                                rows);
                        }
                    }
                    else
                    {
                        return null;
                    }
                }
            };
            
            cir.setReturnValue(provider);
        }
    }
}
