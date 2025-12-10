package xyz.iwolfking.unobtainium.mixin.transmog;

import com.hidoni.transmog.RenderUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import xyz.iwolfking.unobtainium.fixes.TransmogRenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "transmog")
    }
)
@Mixin(value = RenderUtils.class, remap = false)
public class MixinRenderUtils { // https://github.com/Hidoni/Transmog/commit/3e77e8ed975f7a56a38f3a395a234966ff0a316b

    @Inject(method = "isCalledForRendering", at = @At("HEAD"), cancellable = true)
    public void isCalledForRendering(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TransmogRenderUtils.isCalledForRendering());
    }

    @Inject(method = "isCalledForInventory", at = @At("HEAD"), cancellable = true)
    public void isCalledForInventory(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TransmogRenderUtils.isCalledForInventory());
    }

}
