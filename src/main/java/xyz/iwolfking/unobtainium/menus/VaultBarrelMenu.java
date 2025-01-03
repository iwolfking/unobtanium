//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.menus;


import org.jetbrains.annotations.NotNull;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


/**
 * This custom menu allows to prevent shulker box and other vault barrels to be added into barrels.
 */
public class VaultBarrelMenu extends AbstractContainerMenu
{
    private static final int SLOTS_PER_ROW = 9;

    private final Container container;

    private final int containerRows;


    public VaultBarrelMenu(MenuType<?> menuType, int containerID, Inventory playerInventory, Container container, int rows)
    {
        super(menuType, containerID);
        checkContainerSize(container, rows * SLOTS_PER_ROW);
        this.container = container;
        this.containerRows = rows;
        container.startOpen(playerInventory.player);
        int offset = (this.containerRows - 4) * 18;

        for (int index = 0; index < this.containerRows; ++index)
        {
            for (int slot = 0; slot < SLOTS_PER_ROW; ++slot)
            {
                this.addSlot(new ShulkerBoxSlot(container, slot + index * SLOTS_PER_ROW, 8 + slot * 18, 18 + index * 18));
            }
        }

        for (int index = 0; index < 3; ++index)
        {
            for (int slot = 0; slot < SLOTS_PER_ROW; ++slot)
            {
                this.addSlot(new Slot(playerInventory,
                    slot + index * SLOTS_PER_ROW + SLOTS_PER_ROW,
                    8 + slot * 18,
                    103 + index * 18 + offset));
            }
        }

        for (int slot = 0; slot < SLOTS_PER_ROW; ++slot)
        {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 161 + offset));
        }
    }


    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return this.container.stillValid(player);
    }


    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slotIndex)
    {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem())
        {
            ItemStack item = slot.getItem();
            empty = item.copy();

            if (slotIndex < this.containerRows * SLOTS_PER_ROW)
            {
                if (!this.moveItemStackTo(item, this.containerRows * SLOTS_PER_ROW, this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(item, 0, this.containerRows * SLOTS_PER_ROW, false))
            {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return empty;
    }


    public void removed(@NotNull Player player)
    {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
