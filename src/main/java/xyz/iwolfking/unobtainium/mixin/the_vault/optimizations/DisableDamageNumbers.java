package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.entity.entity.VaultDamageNumberEntity;
import iskallia.vault.util.VaultDamageHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Mixin(value = VaultDamageHelper.class, remap = false)
public abstract class DisableDamageNumbers {

    @Shadow
    private static int getAvailablePosition(UUID entityId) {
        return 0;
    }

    @Shadow
    private static double calculateHeightPosition(Entity target) {
        return 0;
    }

    /**
     * @author iwolfking
     * @reason test
     */
    @Overwrite
    public static void spawnDamageNumber(Entity target, float damage, DamageSource source, ServerLevel level) {

    }

    /**
     * @author iwolfking
     * @reason test
     */
    @Overwrite
    public static void spawnCriticalDamageNumber(Entity target, float damage, ServerLevel level) {

    }
}
