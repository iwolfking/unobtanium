package xyz.iwolfking.unobtainium.api.lib.inventory;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * This code is originally from Easy Shulker Boxes.
 * <a href="https://github.com/Fuzss/easyshulkerboxes/tree/1.18">...</a>
 */
public class SimpleContainerWithSlots extends SimpleContainer {
    public SimpleContainerWithSlots(int containerRows) {
        super(containerRows * 9);
    }

    @Override
    public void fromTag(ListTag pContainerNbt) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }
        for(int k = 0; k < pContainerNbt.size(); ++k) {
            CompoundTag compoundtag = pContainerNbt.getCompound(k);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 0 && j < this.getContainerSize()) {
                this.setItem(j, ItemStack.of(compoundtag));
            }
        }

    }

    @Override
    public ListTag createTag() {
        ListTag listtag = new ListTag();

        for(int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }

        return listtag;
    }


    @Override
    public @NotNull ItemStack addItem(ItemStack itemStack)
    {
        // Prevents overstacked items to be inserted in single slot
        if (itemStack.getCount() > itemStack.getMaxStackSize())
        {
            ItemStack copy = itemStack.copy();
            copy.setCount(itemStack.getMaxStackSize());

            // Try to insert normal stack size item
            copy = super.addItem(copy);

            // Reset remaining item stack size
            itemStack.setCount(itemStack.getCount() - itemStack.getMaxStackSize() + copy.getCount());

            return itemStack;
        }

        return super.addItem(itemStack);
    }
}