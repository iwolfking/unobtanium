//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
                        switch (chestEntity.getContainerSize())
                        {
                            case 36 ->
                            {
                                return new ChestMenu(MenuType.GENERIC_9x4,
                                    containerId,
                                    playerInventory,
                                    chestEntity,
                                    4);
                            }
                            case 45 ->
                            {
                                return new ChestMenu(MenuType.GENERIC_9x5,
                                    containerId,
                                    playerInventory,
                                    chestEntity,
                                    5);
                            }
                            case 54 ->
                            {
                                return new ChestMenu(MenuType.GENERIC_9x6,
                                    containerId,
                                    playerInventory,
                                    chestEntity,
                                    6);
                            }
                            default ->
                            {
                                return new ChestMenu(MenuType.GENERIC_9x3,
                                    containerId,
                                    playerInventory,
                                    chestEntity,
                                    3);
                            }
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
