package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import iskallia.vault.client.data.ClientVaultXpTracker;
import iskallia.vault.core.data.key.GenericFieldKey;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.VaultRarity;
import java.lang.reflect.Constructor;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.unobtainium.stat.VaultStatFields;

// make the xp tracker use new vaultstats instead of iterating over the entire list of chests broken to count stats
// returns the original (now empty) list so the loop no-ops
@Mixin(value = ClientVaultXpTracker.class, remap = false)
public abstract class ClientVaultXpTrackerStatsMixin {

    @Shadow @Final private Map<Object, Integer> reusableChestCounts;

    @Shadow @Final private Map<Object, Integer> reusableBarrelCounts;

    private static Constructor<?> unobtanium$chestKeyCtor;

    private static Object unobtanium$chestKey(VaultChestType type, VaultRarity rarity) {
        try {
            if (unobtanium$chestKeyCtor == null) {
                Class<?> keyClass = Class.forName("iskallia.vault.client.data.ClientVaultXpTracker$ChestKey");
                unobtanium$chestKeyCtor = keyClass.getDeclaredConstructor(VaultChestType.class, VaultRarity.class);
                unobtanium$chestKeyCtor.setAccessible(true);
            }
            return unobtanium$chestKeyCtor.newInstance(type, rarity);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct ClientVaultXpTracker.ChestKey via reflection", e);
        }
    }

    @Redirect(
        method = "buildSnapshot(Liskallia/vault/core/vault/stat/StatCollector;F)Liskallia/vault/client/data/ClientVaultXpTracker$XpSnapshot;",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;get(Liskallia/vault/core/data/key/GenericFieldKey;)Ljava/lang/Object;"),
        remap = false
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object unobtanium$fillCountsFromHistogram(StatCollector collector, GenericFieldKey key) {
        if (key == StatCollector.CHESTS) {
            VaultStatFields.forEachLootedChest(collector, (type, rarity, count) ->
                this.reusableChestCounts.merge(unobtanium$chestKey(type, rarity), count, Integer::sum));
        } else if (key == StatCollector.BARRELS) {
            VaultStatFields.forEachLootedBarrel(collector, (type, rarity, count) ->
                this.reusableBarrelCounts.merge(unobtanium$chestKey(type, rarity), count, Integer::sum));
        }
        return collector.get(key);
    }

    @Inject(
        method = "computeChestXp(Liskallia/vault/core/vault/stat/StatCollector;)F",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void unobtanium$augmentChestXp(StatCollector collector, CallbackInfoReturnable<Float> cir) {
        Map<VaultChestType, Map<VaultRarity, Float>> chestXp = ModConfigs.VAULT_STATS.getChests();
        Map<VaultChestType, Map<VaultRarity, Float>> barrelXp = ModConfigs.VAULT_STATS.getBarrels();
        float[] add = {0.0F};

        VaultStatFields.forEachLootedChest(collector, (type, rarity, count) -> {
            Map<VaultRarity, Float> byRarity = chestXp.get(type);
            if (byRarity != null) {
                Float value = byRarity.get(rarity);
                if (value != null) {
                    add[0] += value * count;
                }
            }
        });

        VaultStatFields.forEachLootedBarrel(collector, (type, rarity, count) -> {
            Map<VaultRarity, Float> byRarity = barrelXp.get(type);
            if (byRarity != null) {
                Float value = byRarity.get(rarity);
                if (value != null) {
                    add[0] += value * count;
                }
            }
        });

        if (add[0] != 0.0F) {
            cir.setReturnValue(cir.getReturnValueF() + add[0]);
        }
    }
}
