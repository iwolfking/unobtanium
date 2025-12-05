package xyz.iwolfking.unobtainium.mixin.cit.cititem;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITItem;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "citresewn")
    }
)
@Mixin({ItemStack.class})
public class ItemStackMixin implements CITItem.Cached {
    private WeakReference<CITItem> citresewn_cachedCITItem = null;
    private long citresewn_cacheTimeCITItem = 0L;
    private boolean citresewn_mojankCIT = false;

    @Override
    public CITItem citresewn_getCachedCITItem(Supplier<CITItem> realtime) {
        if (System.currentTimeMillis() - this.citresewn_cacheTimeCITItem >= CITResewnConfig.INSTANCE().cache_ms) {
            var val = realtime.get();
            if (val == null) {
                return null;
            }
            this.citresewn_cachedCITItem = new WeakReference<>(val);
            this.citresewn_cacheTimeCITItem = System.currentTimeMillis();
        }

        var cached = this.citresewn_cachedCITItem;
        if (cached == null) {
            return null;
        }
        return cached.get();
    }

    @Override
    public boolean citresewn_isMojankCIT() {
        return this.citresewn_mojankCIT;
    }

    @Override
    public void citresewn_setMojankCIT(boolean mojankCIT) {
        this.citresewn_mojankCIT = mojankCIT;
    }
}
