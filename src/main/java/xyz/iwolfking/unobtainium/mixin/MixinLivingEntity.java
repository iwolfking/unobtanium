package xyz.iwolfking.unobtainium.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.LivingEntity;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "taterzens")
        }
)
@Mixin(value = LivingEntity.class, priority = 1500)
public class MixinLivingEntity {
    @TargetHandler(mixin = "iskallia.vault.mixin.MixinLivingEntityGrayscale", name = "defineGrayscaleData")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void skipTaters(CallbackInfo ci) {
        if((LivingEntity)(Object)this instanceof TaterzenNPC) {
            ci.cancel();
        }
    }
}
