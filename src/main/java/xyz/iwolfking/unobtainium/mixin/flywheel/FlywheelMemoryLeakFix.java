package xyz.iwolfking.unobtainium.mixin.flywheel;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "flywheel")
    }
)
@Mixin(value = com.jozufozu.flywheel.util.WorldAttached.class, remap = false)
public class FlywheelMemoryLeakFix<T> {
    @Mutable @Shadow @Final private Map<LevelAccessor, T> attached;

    @Shadow static List<WeakReference<Map<LevelAccessor, ?>>> allMaps;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean dontAddHashMap(List<WeakReference<Map<LevelAccessor, ?>>> instance, Object e){
        return false;
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void useWeakHashMap(Function<LevelAccessor, T> factory, CallbackInfo ci){
        this.attached = new WeakHashMap<>();
        allMaps.add(new WeakReference<>(this.attached));
    }
}
