package xyz.iwolfking.unobtainium.mixin.sophbackpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedcore.upgrades.*;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeWrapper;
import org.spongepowered.asm.mixin.*;

import java.util.function.Consumer;

@Mixin(value = VoidUpgradeWrapper.class, remap = false)
public abstract class MixinVoidWrapper extends UpgradeWrapperBase<VoidUpgradeWrapper, VoidUpgradeItem> implements IInsertResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade, IOverflowResponseUpgrade, ISlotLimitUpgrade{
    @Shadow private boolean shouldVoidOverflow;
    @Shadow @Final private FilterLogic filterLogic;

    protected MixinVoidWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }


    @Unique
    private ItemStack unobtainium$emptyVoidedStackReference() {
        ItemStack stack = ItemStack.EMPTY;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("voided", true);
        stack.setTag(tag);
        return stack;
    }

    /**
     * @author iwolfking
     * @reason Replaces void upgrade main logic with one that returns special versions of empty itemstacks that can be used to determine if magnet durability should be taken
     */
    @Overwrite
    public ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate) {
        if (this.shouldVoidOverflow && inventoryHandler.getStackInSlot(slot).isEmpty() && (!this.filterLogic.shouldMatchNbt() || !this.filterLogic.shouldMatchDurability() || this.filterLogic.getPrimaryMatch() != PrimaryMatch.ITEM) && this.filterLogic.matchesFilter(stack)) {
            for (int s = 0; s < inventoryHandler.getSlots(); ++s) {
                if (s != slot && this.stackMatchesFilterStack(inventoryHandler.getStackInSlot(s), stack)) {
                    return unobtainium$emptyVoidedStackReference();
                }
            }

            return stack;
        } else {
            return !this.shouldVoidOverflow && this.filterLogic.matchesFilter(stack) ? unobtainium$emptyVoidedStackReference() : stack;
        }
    }
}
