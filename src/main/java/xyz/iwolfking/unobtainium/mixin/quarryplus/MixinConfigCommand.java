package xyz.iwolfking.unobtainium.mixin.quarryplus;

import com.yogpc.qp.utils.ConfigCommand;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "quarryplus")
    }
)
@Mixin(value = ConfigCommand.class, remap = false)
public class MixinConfigCommand {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void removeBrokenCommands(RegisterCommandsEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
