package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.entity.AnimalPenTileEntity;
import iskallia.vault.init.ModItems;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
    In vanilla Vault Hunters, only sword based items can hit Animal Pens to kill mobs, this also allows any AxeItems to do the same
    by acting like they are a sword.
*/
@Mixin(value = AnimalPenTileEntity.class)
public class FixAxesWithAnimalPen {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0))
    private Item returnSwordForAxe(ItemStack instance) {
        if(instance.getItem() instanceof AxeItem) {
            return new ItemStack(ModItems.SWORD).getItem();
        }
        return instance.getItem();
    }
}
