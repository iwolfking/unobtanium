package xyz.iwolfking.unobtainium.mixin.transmog;

import com.google.common.collect.MapMaker;
import com.hidoni.transmog.TransmogUtils;
import iskallia.vault.gear.item.VaultGearItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentMap;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(value = TransmogUtils.class, remap = false)
public class MixinTransmogUtils {
    @Unique private static ConcurrentMap<ItemStack, ItemStack> transmogPerf$cache = new MapMaker().weakKeys().makeMap();

    @Inject(method = "getAppearanceStackFromItemStack", at = @At("HEAD"), cancellable = true)
    private static void getCachedGearStack(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        var cachedItem = transmogPerf$cache.get(itemStack);
        if (cachedItem != null) {
            cir.setReturnValue(cachedItem);
        }
    }

    @Inject(method = "getAppearanceStackFromItemStack", at = @At("TAIL"))
    private static void storeGearItem(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        var is = cir.getReturnValue();
        if (is.getItem() instanceof VaultGearItem && !transmogPerf$cache.containsKey(itemStack)) {
            transmogPerf$cache.put(itemStack, is);
        }
    }

}
