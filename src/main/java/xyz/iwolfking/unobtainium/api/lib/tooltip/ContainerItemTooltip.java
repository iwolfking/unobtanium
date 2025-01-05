package xyz.iwolfking.unobtainium.api.lib.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * This code is originally from Easy Shulker Boxes.
 * <a href="https://github.com/Fuzss/easyshulkerboxes/tree/1.18">...</a>
 */
public record ContainerItemTooltip(NonNullList<ItemStack> items, int containerRows, @Nullable DyeColor backgroundColor) implements TooltipComponent {

}