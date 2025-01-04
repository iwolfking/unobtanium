package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import fuzs.easyshulkerboxes.world.item.ContainerItemHelper;
import iskallia.vault.block.VaultBarrelBlock;
import iskallia.vault.init.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;


/**
 * This mixin is copy of fuzs.easyshulkerboxes.mixin.ItemMixin from Easy ShulkerBoxes.
 * It is adjusted so it would work with Vault Barrels.
 */
@Mixin(value = Item.class)
public abstract class FixVaultBarrelItemInsertRemove
{
    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    public void overrideStackedOnOther(ItemStack stack,
        Slot slot,
        ClickAction clickAction,
        Player player,
        CallbackInfoReturnable<Boolean> callbackInfo)
    {
        if (Block.byItem(stack.getItem()) instanceof VaultBarrelBlock)
        {
            boolean success = ContainerItemHelper.overrideStackedOnOther(stack,
                ModBlocks.VAULT_CHEST_TILE_ENTITY,
                unobtainium$getSize(stack),
                slot,
                clickAction,
                player,
                (s) -> s.getItem().canFitInsideContainerItems(),
                SoundEvents.BUNDLE_INSERT,
                SoundEvents.BUNDLE_REMOVE_ONE);

            callbackInfo.setReturnValue(success);
        }
    }


    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack stack,
        ItemStack stackOnMe,
        Slot slot,
        ClickAction clickAction,
        Player player,
        SlotAccess slotAccess,
        CallbackInfoReturnable<Boolean> callbackInfo)
    {
        if (Block.byItem(stack.getItem()) instanceof VaultBarrelBlock)
        {
            boolean success = ContainerItemHelper.overrideOtherStackedOnMe(stack,
                ModBlocks.VAULT_CHEST_TILE_ENTITY,
                unobtainium$getSize(stack),
                stackOnMe,
                slot,
                clickAction,
                player,
                slotAccess,
                (s) -> s.getItem().canFitInsideContainerItems(),
                SoundEvents.BUNDLE_INSERT,
                SoundEvents.BUNDLE_REMOVE_ONE);
            callbackInfo.setReturnValue(success);
        }
    }


    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> callbackInfo)
    {
        if (Block.byItem(stack.getItem()) instanceof VaultBarrelBlock)
        {
            Optional<TooltipComponent> component = ContainerItemHelper.getTooltipImage(stack,
                ModBlocks.VAULT_CHEST_TILE_ENTITY,
                unobtainium$getSize(stack),
                DyeColor.LIGHT_GRAY);
            callbackInfo.setReturnValue(component);
        }
    }


    @Unique
    private static int unobtainium$getSize(ItemStack itemStack)
    {
        // Wooden barrel has 4 rows while other barrels has 5. No need to check all of them.
        return itemStack.is(ModBlocks.WOODEN_BARREL_ITEM) ? 4 : 5;
    }
}