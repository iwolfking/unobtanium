package xyz.iwolfking.unobtainium.mixin.cit.citelytra;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITElytra;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "citresewn")
    }
)
@Mixin({ItemStack.class})
public class ItemStackMixin implements CITElytra.Cached {
    private WeakReference<CITElytra> citresewn_cachedCITElytra = null;
    private long citresewn_cacheTimeCITElytra = 0L;

    @Override
    public CITElytra citresewn_getCachedCITElytra(Supplier<CITElytra> realtime) {
        if (System.currentTimeMillis() - this.citresewn_cacheTimeCITElytra >= CITResewnConfig.INSTANCE().cache_ms) {
            CITElytra val = realtime.get();
            if (val == null) {
                return null;
            }
            this.citresewn_cachedCITElytra = new WeakReference<>(val);
            this.citresewn_cacheTimeCITElytra = System.currentTimeMillis();
        }
        var cached = this.citresewn_cachedCITElytra;
        if (cached == null) {
            return null;
        }
        return cached.get();
    }
}
