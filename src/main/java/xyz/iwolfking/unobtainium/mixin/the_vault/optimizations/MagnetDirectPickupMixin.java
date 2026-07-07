package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.item.MagnetItem;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MagnetItem.class, remap = false)
public abstract class MagnetDirectPickupMixin {

    @Shadow
    private static boolean allowsNoPickupDelay(ItemEntity itemEntity, Player player) {
        return false;
    }

    @Inject(method = "teleportToPlayer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void unobtainium$directPickup(Player player, List<? extends Entity> entities, CallbackInfo ci) {
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item) {
                if (allowsNoPickupDelay(item, player)) {
                    item.setNoPickUpDelay();
                }
                item.getTags().add(MagnetItem.PULLED);
            }
            entity.playerTouch(player);
        }
        ci.cancel();
    }

    @Inject(method = "moveToPlayer", at = @At("HEAD"), remap = false)
    private static void unobtainium$skipStuck(Player player, List<? extends Entity> entities, float speed, CallbackInfo ci) {
        entities.removeIf(entity -> entity.isRemoved() || entity.getBoundingBox().intersects(player.getBoundingBox()));
    }
}
