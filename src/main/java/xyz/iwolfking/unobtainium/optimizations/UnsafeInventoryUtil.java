package xyz.iwolfking.unobtainium.optimizations;

import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Reading stacks from this class is same as InventoryUtil, but it's faster.
 * Modifying the stacks is undefined behavior. Don't do it.
 * It gives you direct pointer to the underlying stacks to avoid copying.
 * Only use it if you need to check if player has some item (like legendary bounty) or to count items (scav overlay)
 * If you're not reading player's inventory on every frame/tick, use InventoryUtil instead.
 */

public class UnsafeInventoryUtil {
    private static final Set<Function<Player, List<ItemStack>>> INVENTORY_ACCESS =
        Set.of(player -> {
            List<ItemStack> items = new ArrayList<>();
            Inventory inv = player.getInventory();

            for (int slot = 0; slot < inv.getContainerSize(); slot++) {
                ItemStack stack = inv.getItem(slot);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }

            return items;
        }, player -> {
            List<ItemStack> items = new ArrayList<>();
            IntegrationCurios.getCuriosItemStacks(player).forEach((slot, stackTpl) -> stackTpl.forEach(tpl -> {
                ItemStack stack = tpl.getA();
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }));
            return items;
        });
    private static final Set<Function<Player, List<ItemStack>>> MAIN_HAND_ACCESS =
        Set.of(player -> {
            List<ItemStack> items = new ArrayList<>();
            ItemStack mainHandItem = player.getMainHandItem();
            if (!mainHandItem.isEmpty()) {
                items.add(mainHandItem);
            }

            return items;
        });
    private static final Set<Function<ItemStack, List<ItemStack>>>
        CONTENT_ACCESSORS =
        Set.of(
            UnsafeInventoryUtil::getReadOnlyBackpackItemAccess,
            UnsafeInventoryUtil::getShulkerBoxAccess,
            UnsafeInventoryUtil::getBundleItemAccess,
            UnsafeInventoryUtil::getSatchelItemAccess,
            UnsafeInventoryUtil::getSupplementariesSafeAccess,
            UnsafeInventoryUtil::getSupplementariesSackAccess,
            UnsafeInventoryUtil::getBotaniaBaubleBoxAccess
        );

    public static List<ItemStack> findAllItems(Player player) {
        List<ItemStack> itemAccesses = new ArrayList<>();

        for (Function<Player, List<ItemStack>> inventoryFn : INVENTORY_ACCESS) {
            inventoryFn.apply(player)
                .forEach(inventoryStackAccess -> discoverContents(inventoryStackAccess, itemAccesses));
        }

        return itemAccesses;
    }

    public static List<ItemStack> findAllItemsInMainHand(Player player) {
        List<ItemStack> itemAccesses = new ArrayList<>();

        for (Function<Player, List<ItemStack>> mainHandFn : MAIN_HAND_ACCESS) {
            mainHandFn.apply(player)
                .forEach(mainHandStackAccess -> discoverContents(mainHandStackAccess, itemAccesses));
        }

        return itemAccesses;
    }


    private static void discoverContents(ItemStack access, List<ItemStack> out) {
        out.add(access);

        for (Function<ItemStack, List<ItemStack>> containerAccess : CONTENT_ACCESSORS) {
            containerAccess.apply(access).forEach(containedAccess -> discoverContents(containedAccess, out));
        }
    }

    public static List<ItemStack> findAllItems(List<ItemStack> items) {
        List<ItemStack> itemAccesses = new ArrayList<>();
        for (ItemStack stack : items) {
            itemAccesses.add(stack);

            for (Function<ItemStack, List<ItemStack>> containerAccess : CONTENT_ACCESSORS) {
                containerAccess.apply(stack)
                    .forEach(containedAccess -> discoverContents(containedAccess, itemAccesses));
            }
        }

        return itemAccesses;
    }


    private static List<ItemStack> getBotaniaBaubleBoxAccess(ItemStack container) {
        var regName = container.getItem().getRegistryName();
        if (regName.getNamespace().equals("botania") && regName.getPath().equals("bauble_box") && container.hasTag()) {
            List<ItemStack> accesses = new ArrayList<>();
            ListTag itemList = container.getOrCreateTag().getList("Items", 10);

            for (int slot = 0; slot < itemList.size(); slot++) {
                ItemStack storedItem = ItemStack.of(itemList.getCompound(slot));
                if (!storedItem.isEmpty()) {
                    accesses.add(storedItem);
                }
            }
            return accesses;
        }

        return Collections.emptyList();
    }

    private static List<ItemStack> getSupplementariesSackAccess(ItemStack container) {
        var regName = container.getItem().getRegistryName();
        if (regName.getNamespace().equals("supplementaries") && regName.getPath().equals("sack") && container.hasTag()) {
            List<ItemStack> accesses = new ArrayList<>();
            CompoundTag tag = BlockItem.getBlockEntityData(container);
            if (tag != null) {
                NonNullList<ItemStack> contents = NonNullList.withSize(9, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag, contents);

                for (int slot = 0; slot < contents.size(); slot++) {
                    ItemStack stack = contents.get(slot);
                    if (!stack.isEmpty()) {
                        accesses.add(stack);
                    }
                }
            }
            return accesses;
        }

        return Collections.emptyList();
    }

    private static List<ItemStack> getSupplementariesSafeAccess(ItemStack container) {
        var regName = container.getItem().getRegistryName();
        if (regName.getNamespace().equals("supplementaries") && regName.getNamespace().equals("safe") && container.hasTag()) {
            List<ItemStack> accesses = new ArrayList<>();
            CompoundTag tag = BlockItem.getBlockEntityData(container);
            if (tag != null) {
                NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag, contents);

                for (int slot = 0; slot < contents.size(); slot++) {
                    ItemStack stack = contents.get(slot);
                    if (!stack.isEmpty()) {
                        accesses.add(stack);
                    }
                }
            }
            return accesses;
        }

        return Collections.emptyList();
    }

    private static List<ItemStack> getSatchelItemAccess(ItemStack container) {
        var regName = container.getItem().getRegistryName();
        if (regName.getNamespace().equals("thermal") && regName.getPath().equals("satchel") && container.hasTag()) {
            List<ItemStack> accesses = new ArrayList<>();
            CompoundTag invTag = container.getOrCreateTagElement("ItemInv");
            if (invTag.contains("ItemInv", 9)) {
                ListTag list = invTag.getList("ItemInv", 10);

                for (int slot = 0; slot < list.size(); slot++) {
                    ItemStack stack = ItemStack.of(list.getCompound(slot));
                    if (!stack.isEmpty()) {
                        accesses.add(stack);
                    }
                }
            }
            return accesses;
        }

        return Collections.emptyList();
    }

    private static List<ItemStack> getBundleItemAccess(ItemStack container) {
        if (container.getItem() instanceof BundleItem && container.hasTag()) {
            List<ItemStack> accesses = new ArrayList<>();
            CompoundTag tag = container.getOrCreateTag();
            ListTag itemList = tag.getList("Items", 10);

            for (int slot = 0; slot < itemList.size(); slot++) {
                CompoundTag itemTag = itemList.getCompound(slot);
                ItemStack itemStack = ItemStack.of(itemTag);
                if (!itemStack.isEmpty()) {
                    accesses.add(itemStack);
                }
            }

            return accesses;
        }
        return Collections.emptyList();
    }

    private static List<ItemStack> getShulkerBoxAccess(ItemStack container) {
        if (isShulkerBox(container.getItem())) {
            List<ItemStack> accesses = new ArrayList<>();
            CompoundTag tag = BlockItem.getBlockEntityData(container);
            if (tag != null) {
                int invSize = 27;
                // BONNe barrel patch
                if (container.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultBarrelBlock) {
                    if (container.is(ModBlocks.WOODEN_BARREL.asItem())) {
                        invSize = 36;
                    } else {
                        invSize = 45;
                    }
                }

                NonNullList<ItemStack> contents = NonNullList.withSize(invSize, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag, contents);

                for (int slot = 0; slot < contents.size(); slot++) {
                    ItemStack stack = contents.get(slot);
                    if (!stack.isEmpty()) {
                        accesses.add(stack);
                    }
                }
            }
            return accesses;
        }

        return Collections.emptyList();
    }

    private static boolean isShulkerBox(Item item) {
        return item instanceof BlockItem blockItem && (blockItem.getBlock() instanceof ShulkerBoxBlock || blockItem.getBlock() instanceof VaultBarrelBlock);
    }


    public static List<ItemStack> getReadOnlyBackpackItemAccess(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            // avoid copying the item if it's not a backpack
            return Collections.emptyList();
        }
        // copy is required here if we need the data on client
        return backpack.copy().getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).map((wrapper) -> {
            List<ItemStack> accesses = new ArrayList<>();
            InventoryHandler invHandler = wrapper.getInventoryHandler();

            for(int slot = 0; slot < invHandler.getSlots(); ++slot) {
                ItemStack slotStack = invHandler.getStackInSlot(slot);
                if (!slotStack.isEmpty()) {
                    accesses.add(slotStack);
                }
            }

            return accesses;
        }).orElse(Collections.emptyList());
    }
}