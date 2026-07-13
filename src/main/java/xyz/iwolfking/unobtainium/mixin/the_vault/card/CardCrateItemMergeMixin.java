package xyz.iwolfking.unobtainium.mixin.the_vault.card;

import iskallia.vault.core.card.ActiveCardTaskHelper;
import iskallia.vault.core.data.compound.ItemStackList;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ActiveCardTaskHelper.class, remap = false)
public class CardCrateItemMergeMixin {

    @Redirect(
        method = "lambda$onServerTick$2(Ljava/util/UUID;ILiskallia/vault/core/card/ActiveCardTaskHelper$TaskEntry;ZFLiskallia/vault/core/vault/Vault;)V",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/data/compound/ItemStackList;addAll(Ljava/util/Collection;)Z"),
        remap = false
    )
    private static boolean unobtanium$mergeCrateItems(ItemStackList list, Collection<ItemStack> loot) {
        outerloop: for (ItemStack stack : loot) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            for (ItemStack existing : list) {
                if (existing != null && !existing.isEmpty() && ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                    existing.setCount(existing.getCount() + stack.getCount());
                    continue outerloop;
                }
            }

            list.add(stack.copy());
        }

        return true;
    }
}
