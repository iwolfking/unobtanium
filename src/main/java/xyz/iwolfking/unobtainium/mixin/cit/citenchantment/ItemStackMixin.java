package xyz.iwolfking.unobtainium.mixin.cit.citenchantment;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITEnchantment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Supplier;
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "citresewn")
    }
)
@Mixin({ItemStack.class})
public class ItemStackMixin implements CITEnchantment.Cached {
    private WeakReference<List<CITEnchantment>> citresewn_cachedCITEnchantment = null;
    private long citresewn_cacheTimeCITEnchantment = 0L;

    @Override
    public List<CITEnchantment> citresewn_getCachedCITEnchantment(Supplier<List<CITEnchantment>> realtime) {
        if (System.currentTimeMillis() - this.citresewn_cacheTimeCITEnchantment >= CITResewnConfig.INSTANCE().cache_ms) {

            List<CITEnchantment> val = realtime.get();
            if (val == null) {
                return null;
            }
            this.citresewn_cachedCITEnchantment = new WeakReference<>(val);
            this.citresewn_cacheTimeCITEnchantment = System.currentTimeMillis();
        }
        var cached = this.citresewn_cachedCITEnchantment;
        if (cached == null) {
            return null;
        }
        return cached.get();
    }

    @Inject(
        method = {"hasFoil"},
        cancellable = true,
        at = {@At("HEAD")}
    )
    private void disableDefaultGlint(CallbackInfoReturnable<Boolean> cir) {
        if (CITResewn.INSTANCE.activeCITs != null
            && (
            !CITResewn.INSTANCE.activeCITs.effectiveGlobalProperties.useGlint
                || CITEnchantment.appliedContext != null && CITEnchantment.shouldApply && !(CITEnchantment.appliedContext.get(0)).useGlint
        )) {
            cir.setReturnValue(false);
        }
    }
}
