package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.bounty.client.ClientBountyData;
import iskallia.vault.init.ModItems;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Only calculate lost bounty info if someone asks for it.
 * It will now only run when you're looking at the bounty table instead of hurting perf in vault.
 */
@Mixin(value = ClientBountyData.class, remap = false)
public class OptimizeClientBountyData {

//    @Shadow private static boolean hasLostBountyInInventory;
//    @Unique private static long unobtainium$lastLostBountyTick = -1;
//
//    @Redirect(method = "onClientTick", at = @At(value = "INVOKE", target = "Liskallia/vault/util/InventoryUtil;findAllItems(Lnet/minecraft/world/entity/player/Player;)Ljava/util/List;"))
//    private static List<InventoryUtil.ItemAccess> onClientTick(Player inventoryFn) {
//        return List.of();
//    }
//
//    @Inject(method = "hasLostBountyInInventory", at = @At("HEAD"))
//    private static void hasLostBountyInInventory(CallbackInfoReturnable<Boolean> cir) {
//        unobtainium$updateLostBounty();
//    }
//
//    @Unique private static void unobtainium$updateLostBounty() {
//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player == null) {
//            return;
//        }
//        if (player.tickCount != unobtainium$lastLostBountyTick) {
//            unobtainium$lastLostBountyTick = player.tickCount;
//            hasLostBountyInInventory = InventoryUtil.findAllItems(player).stream().anyMatch(stack -> stack.getItem() == ModItems.LOST_BOUNTY);
//        }
//    }
}
