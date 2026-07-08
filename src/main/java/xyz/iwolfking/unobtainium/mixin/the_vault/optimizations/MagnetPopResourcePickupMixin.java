package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.item.MagnetItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.unobtainium.magnet.MagnetSpawnPickup;

// fast-path for instant magnet
@Mixin(Block.class)
public abstract class MagnetPopResourcePickupMixin {

    @Redirect(
        method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private static boolean unobtainium$magnetFastPickup(Level level, Entity entity) {
        if (level instanceof ServerLevel serverLevel
                && entity instanceof ItemEntity item
                && !item.getItem().isEmpty()
                && !item.getTags().contains(MagnetItem.BLACKLIST)
                && MagnetSpawnPickup.tryPickup(serverLevel, item)) {
            return false;
        }
        return level.addFreshEntity(entity);
    }
}
