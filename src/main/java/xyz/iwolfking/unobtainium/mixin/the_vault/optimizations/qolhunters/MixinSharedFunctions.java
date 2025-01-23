package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations.qolhunters;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.iridium.qolhunters.util.SharedFunctions;
import iskallia.vault.util.InventoryUtil;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.unobtainium.optimizations.UnsafeInventoryUtil;

import java.util.Iterator;
import java.util.List;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "qolhunters")
    }
)
@Mixin(value = SharedFunctions.class, remap = false)
public class MixinSharedFunctions {
    @Redirect(method = "GetPlayerInventoryItems", at = @At(value = "INVOKE", target = "Liskallia/vault/util/InventoryUtil;findAllItems(Lnet/minecraft/world/entity/player/Player;)Ljava/util/List;"))
    private static List<InventoryUtil.ItemAccess> GetPlayerInventoryItems(Player player, @Share("player") LocalRef<Player> playerRef) {
        playerRef.set(player);
        return List.of();
    }

    @Redirect(method = "GetPlayerInventoryItems", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private static Iterator<?> GetPlayerInventoryItems(List<InventoryUtil.ItemAccess> list, @Share("player") LocalRef<Player> playerRef, @Share("iterator") LocalRef<Iterator<ItemStack>> listRef) {
        Iterator<ItemStack> iterator = UnsafeInventoryUtil.findAllItems(playerRef.get()).iterator();
        listRef.set(iterator);
        return iterator;
    }

    @Redirect(method = "GetPlayerInventoryItems", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private static Object GetPlayerInventoryItems(Iterator<?> iterator) {
        return null;
    }

    @Redirect(method = "GetPlayerInventoryItems", at = @At(value = "INVOKE", target = "Liskallia/vault/util/InventoryUtil$ItemAccess;getStack()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack GetPlayerInventoryItems(InventoryUtil.ItemAccess itemAccess, @Share("iterator") LocalRef<Iterator<ItemStack>> listRef) {
        return listRef.get().next();
    }
}
