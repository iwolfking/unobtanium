package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.init.ModItems;
import iskallia.vault.item.MagnetItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Set;

/**
 * Magnets stupidly take durability even though the item being picked up is air, they are also not supposed to take durability picking up Soul Shards but the Magnet item itself is what is checked for this...
 */
@Mixin(value = MagnetItem.class, remap = false)
public abstract class FixMagnetDurability {

    @Shadow
    public static Optional<ItemStack> getMagnet(LivingEntity entity) {
        return null;
    }

    @Shadow
    protected static void spawnItemParticles(Entity entity, ItemStack stack, int count) {
    }

    /**
     * @author iwolfking
     * @reason Just replace the whole thing since a Redirect would be messy.
     */
    @Overwrite
    public static void onPlayerPickup(Player player, ItemEntity item) {
        if (item.getTags().contains("MagnetPulled")) {
            getMagnet(player).ifPresent((stack) -> {
                //If item was voided by void upgrade, don't take any durability
                if(item.getItem().getOrCreateTag().getBoolean("voided")) {
                    return;
                }
                if (!item.getItem().is(ModItems.SOUL_SHARD)) {
                    stack.hurtAndBreak(1, player, (entity) -> {
                        if (!entity.isSilent()) {
                            entity.level.playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK, entity.getSoundSource(), 0.8F, 0.8F + entity.level.random.nextFloat() * 0.4F);
                        }

                        spawnItemParticles(entity, stack, 5);
                    });
                }

            });
        }
    }
}
