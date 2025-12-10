package xyz.iwolfking.unobtainium.mixin.cit.citarmor;


import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITArmor;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "citresewn")
    }
)
@Mixin({ItemStack.class})
public class ItemStackMixin implements CITArmor.Cached {
    private WeakReference<CITArmor> citresewn_cachedCITArmor = null;
    private long citresewn_cacheTimeCITArmor = 0L;

    @Override
    public CITArmor citresewn_getCachedCITArmor(Supplier<CITArmor> realtime) {
        if (System.currentTimeMillis() - this.citresewn_cacheTimeCITArmor >= CITResewnConfig.INSTANCE().cache_ms) {

            CITArmor val = realtime.get();
            if (val == null) {
                return null;
            }
            this.citresewn_cachedCITArmor = new WeakReference<>(val);
            this.citresewn_cacheTimeCITArmor = System.currentTimeMillis();
        }
        var cached = this.citresewn_cachedCITArmor;
        if (cached == null) {
            return null;
        }
        return cached.get();
    }
}
