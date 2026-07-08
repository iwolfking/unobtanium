package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import iskallia.vault.core.event.common.ChestGenerationEvent;
import iskallia.vault.core.vault.ClassicLootLogic;
import iskallia.vault.core.vault.stat.ChestStat;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.unobtainium.stat.VaultStatFields;

// re-routes trapped-chest stats
@Mixin(value = ClassicLootLogic.class, remap = false)
public class ClassicLootLogicTrapMixin {

    @Redirect(
        method = "lambda$applyTrap$3(Liskallia/vault/core/event/common/ChestGenerationEvent$Data;Liskallia/vault/core/vault/stat/StatCollector;)V",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/ChestStat$List;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    private static boolean unobtanium$recordTrappedChest(ChestStat.List list, Object element, ChestGenerationEvent.Data data, StatCollector collector) {
        ChestStat stat = (ChestStat) element;
        VaultChestType type = stat.get(ChestStat.TYPE);
        VaultRarity rarity = stat.has(ChestStat.RARITY) ? stat.get(ChestStat.RARITY) : null;
        VaultStatFields.recordChest(collector, type, rarity, stat.has(ChestStat.TRAPPED));
        return true;
    }
}
