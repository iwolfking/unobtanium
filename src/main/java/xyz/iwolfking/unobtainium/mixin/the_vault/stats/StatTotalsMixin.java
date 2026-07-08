package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.data.key.GenericFieldKey;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatTotals;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.unobtainium.stat.IStatTotalsHistogram;
import xyz.iwolfking.unobtainium.stat.VaultStatFields;

@Mixin(value = StatTotals.class, remap = false)
public abstract class StatTotalsMixin implements IStatTotalsHistogram {

    @Shadow @Final private Object2IntMap<StatTotals.ChestKey> lootedChests;

    @Shadow @Final private Object2IntMap<VaultChestType> trappedChests;

    @Override
    public void unobtanium$recordLootedChest(VaultChestType type, VaultRarity rarity, int count) {
        this.lootedChests.computeInt(new StatTotals.ChestKey(type, rarity), (k, v) -> (v == null ? 0 : v) + count);
    }

    @Override
    public void unobtanium$recordTrappedChest(VaultChestType type, int count) {
        this.trappedChests.computeInt(type, (k, v) -> (v == null ? 0 : v) + count);
    }

    @Redirect(
        method = "of(Ljava/util/UUID;)Liskallia/vault/core/vault/stat/StatTotals;",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;get(Liskallia/vault/core/data/key/GenericFieldKey;)Ljava/lang/Object;"),
        remap = false
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object unobtanium$fillTotalsFromHistogram(StatCollector collector, GenericFieldKey key, @Local StatTotals statTotals) {
        if (key == StatCollector.CHESTS) {
            IStatTotalsHistogram sink = (IStatTotalsHistogram) (Object) statTotals;
            VaultStatFields.forEachLootedChest(collector, sink::unobtanium$recordLootedChest);
            VaultStatFields.forEachTrappedChest(collector, sink::unobtanium$recordTrappedChest);
        }
        return collector.get(key); // return the (now empty) list so the vanilla loop no-ops
    }
}
