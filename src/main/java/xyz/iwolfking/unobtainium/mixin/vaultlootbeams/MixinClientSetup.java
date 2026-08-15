package xyz.iwolfking.unobtainium.mixin.vaultlootbeams;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.justahuman.vaultlootbeams.client.ClientSetup;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vaultlootbeams")
    }
)
@Mixin(value = ClientSetup.class, remap = false)
public class MixinClientSetup {
    @Inject(method = "onItemCreation", at = @At("HEAD"), cancellable = true)
    private static void dontCacheEverything(EntityJoinWorldEvent event, CallbackInfo ci){
        ci.cancel();
    }
}
