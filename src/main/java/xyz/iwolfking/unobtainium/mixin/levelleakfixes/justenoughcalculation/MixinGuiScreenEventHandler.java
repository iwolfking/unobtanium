package xyz.iwolfking.unobtainium.mixin.levelleakfixes.justenoughcalculation;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.towdium.jecalculation.events.GuiScreenEventHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jecalculation")
        }
)
@Mixin(value = GuiScreenEventHandler.class, remap = false)
public class MixinGuiScreenEventHandler {
    @ModifyArg(method = "onGuiOpen", at = @At(value = "INVOKE", target = "Lme/towdium/jecalculation/events/GuiScreenOverlayHandler;<init>(Lnet/minecraft/world/entity/player/Inventory;)V"))
    private Inventory nullPlayer(Inventory inventory){
        return null;
    }
}
