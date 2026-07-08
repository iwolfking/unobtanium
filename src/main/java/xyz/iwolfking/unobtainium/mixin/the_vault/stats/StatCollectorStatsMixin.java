package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import iskallia.vault.core.vault.stat.BarrelStats;
import iskallia.vault.core.vault.stat.ChestStat;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.unobtainium.stat.VaultStatFields;

// aggregates chest/barrel stats instead of endlessly appending to a DataList
@Mixin(value = StatCollector.class, remap = false)
public class StatCollectorStatsMixin {

    @Redirect(
        method = "lambda$initServer$0(Ljava/util/UUID;Liskallia/vault/core/event/common/ChestGenerationEvent$Data;)V",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/ChestStat$List;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    private boolean unobtanium$recordChest(ChestStat.List list, Object element) {
        ChestStat stat = (ChestStat) element;
        VaultChestType type = stat.get(ChestStat.TYPE);
        VaultRarity rarity = stat.has(ChestStat.RARITY) ? stat.get(ChestStat.RARITY) : null;
        VaultStatFields.recordChest((StatCollector) (Object) this, type, rarity, stat.has(ChestStat.TRAPPED));
        return true; // swallow the append
    }

    @Redirect(
        method = "lambda$initServer$0(Ljava/util/UUID;Liskallia/vault/core/event/common/ChestGenerationEvent$Data;)V",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/BarrelStats$List;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    private boolean unobtanium$recordBarrel(BarrelStats.List list, Object element) {
        BarrelStats stat = (BarrelStats) element;
        VaultChestType type = stat.get(BarrelStats.TYPE);
        VaultRarity rarity = stat.has(BarrelStats.RARITY) ? stat.get(BarrelStats.RARITY) : null;
        VaultStatFields.recordBarrel((StatCollector) (Object) this, type, rarity);
        return true;
    }

    @Inject(
        method = "getLootedChests(Liskallia/vault/core/vault/stat/VaultChestType;Liskallia/vault/util/VaultRarity;)I",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void unobtanium$augmentLootedChests(VaultChestType type, VaultRarity rarity, CallbackInfoReturnable<Integer> cir) {
        int add = VaultStatFields.chestCount((StatCollector) (Object) this, type, rarity, false);
        if (add != 0) {
            cir.setReturnValue(cir.getReturnValueI() + add);
        }
    }

    @Inject(method = "getLootedChests()Ljava/util/Map;", at = @At("RETURN"), remap = false)
    private void unobtanium$augmentLootedChestsMap(CallbackInfoReturnable<Map<VaultChestType, Map<VaultRarity, Integer>>> cir) {
        Map<VaultChestType, Map<VaultRarity, Integer>> map = cir.getReturnValue();
        VaultStatFields.forEachLootedChest(
            (StatCollector) (Object) this,
            (type, rarity, count) -> map.computeIfAbsent(type, t -> new HashMap<>()).merge(rarity, count, Integer::sum)
        );
    }

    @Inject(
        method = "getTrappedChests(Liskallia/vault/core/vault/stat/VaultChestType;)I",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void unobtanium$augmentTrappedChests(VaultChestType type, CallbackInfoReturnable<Integer> cir) {
        int add = VaultStatFields.trappedCount((StatCollector) (Object) this, type);
        if (add != 0) {
            cir.setReturnValue(cir.getReturnValueI() + add);
        }
    }

    @Inject(
        method = "getLootedBarrels(Liskallia/vault/core/vault/stat/VaultChestType;Liskallia/vault/util/VaultRarity;)I",
        at = @At("RETURN"), cancellable = true, remap = false
    )
    private void unobtanium$augmentLootedBarrels(VaultChestType type, VaultRarity rarity, CallbackInfoReturnable<Integer> cir) {
        int add = VaultStatFields.barrelCount((StatCollector) (Object) this, type, rarity);
        if (add != 0) {
            int base = cir.getReturnValueI();
            cir.setReturnValue((base < 0 ? 0 : base) + add);
        }
    }
}
