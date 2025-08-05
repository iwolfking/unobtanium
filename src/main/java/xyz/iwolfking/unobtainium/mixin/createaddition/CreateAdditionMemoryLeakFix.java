package xyz.iwolfking.unobtainium.mixin.createaddition;

import com.mrh0.createaddition.energy.network.EnergyNetworkManager;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "createaddition")
    }
)
@Mixin(value = EnergyNetworkManager.class, remap = false)
public class CreateAdditionMemoryLeakFix {
    @Shadow public static Map<LevelAccessor, EnergyNetworkManager> instances;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void useWeakHashMap(CallbackInfo ci){
        instances = new WeakHashMap<>();
    }

}
