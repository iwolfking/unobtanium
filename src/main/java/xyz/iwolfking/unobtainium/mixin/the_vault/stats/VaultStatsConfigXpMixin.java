package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import iskallia.vault.config.VaultStatsConfig;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.unobtainium.stat.VaultStatFields;
import xyz.iwolfking.unobtainium.stat.VaultStatHistogram;

@Mixin(value = VaultStatsConfig.class, remap = false)
public class VaultStatsConfigXpMixin {

    @Shadow
    private Map<VaultChestType, Map<VaultRarity, Float>> chests;

    @Shadow
    private Map<VaultChestType, Map<VaultRarity, Float>> barrels;

    @Inject(
        method = "getStatsExperience(Liskallia/vault/core/vault/stat/StatCollector;)I",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void unobtanium$augmentStatsExperience(StatCollector stats, CallbackInfoReturnable<Integer> cir) {
        float[] exp = {0.0F};

        VaultStatFields.forEachLootedChest(stats, (type, rarity, count) -> {
            Map<VaultRarity, Float> byRarity = this.chests.get(type);
            if (byRarity != null) {
                Float value = byRarity.get(rarity);
                if (value != null) {
                    exp[0] += value * count;
                }
            }
        });

        VaultStatHistogram barrelHistogram = VaultStatFields.barrels(stats);
        if (barrelHistogram != null && this.barrels != null) {
            for (Map.Entry<Integer, Integer> entry : barrelHistogram.entrySet()) {
                int key = entry.getKey();
                Map<VaultRarity, Float> byRarity = this.barrels.get(VaultStatFields.barrelType(key));
                if (byRarity != null) {
                    Float value = byRarity.get(VaultStatFields.barrelRarity(key));
                    if (value != null) {
                        exp[0] += value * entry.getValue();
                    }
                }
            }
        }

        if (exp[0] != 0.0F) {
            cir.setReturnValue(cir.getReturnValueI() + (int) exp[0]);
        }
    }
}
