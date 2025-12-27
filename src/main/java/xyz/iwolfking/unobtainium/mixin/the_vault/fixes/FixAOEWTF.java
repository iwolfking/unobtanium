package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.event.PlayerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PlayerEvents.class, remap = false)
public class FixAOEWTF {
    @ModifyConstant(method = "lambda$static$5", constant = @Constant(floatValue = 0.0F), remap = false)
    private static float fixThatShit(float constant) {
        return 1F;
    }
}
