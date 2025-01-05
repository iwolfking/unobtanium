package xyz.iwolfking.unobtainium.api.lib.inventory;

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
    public void fromTag(ListTag p_40108_) {
        for(int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }
        for(int k = 0; k < p_40108_.size(); ++k) {
            CompoundTag compoundtag = p_40108_.getCompound(k);
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
}