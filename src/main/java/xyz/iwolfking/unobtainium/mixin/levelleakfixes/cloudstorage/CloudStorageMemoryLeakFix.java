package xyz.iwolfking.unobtainium.mixin.levelleakfixes.cloudstorage;

import com.github.alexthe668.cloudstorage.CommonProxy;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "cloudstorage")
    }
)
@Mixin(value = CommonProxy.class, remap = false)
public class CloudStorageMemoryLeakFix {
    @Inject(method = "onServerTick", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0), cancellable = true)
    private void dontStoreDimsWithoutSky(TickEvent.WorldTickEvent tick, CallbackInfo ci, @Local ServerLevel level){
        // it won't do anything in dims without skylight, so why should we store them?
        if (!level.dimensionType().hasSkyLight()){
            ci.cancel();
        }
    }
}
