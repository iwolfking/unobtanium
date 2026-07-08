package xyz.iwolfking.unobtainium.mixin.the_vault.stats;

import iskallia.vault.core.data.DataList;
import iskallia.vault.core.vault.stat.BarrelStats;
import iskallia.vault.core.vault.stat.ChestStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

// one-way migration for vaults saved before the chests/barrels change
// and an increase to the (arbitrary) DataList read cap (which was causing de-vaultification)
@Mixin(value = DataList.class, remap = false)
public class VestigialStatListReadMixin {

    @ModifyConstant(method = "read", constant = @Constant(intValue = 100000), remap = false)
    private int unobtanium$raiseReadCap(int original) {
        return 5_000_000;
    }

    @Redirect(
        method = "read",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/data/DataList;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean unobtanium$discardVestigialStatEntries(DataList list, Object element) {
        if (list instanceof ChestStat.List || list instanceof BarrelStats.List) {
            return true; // buffer already advanced by the adapter read, just don't store it
        }
        return list.add(element);
    }
}
