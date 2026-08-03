package xyz.iwolfking.unobtainium.mixin.emojiful;

import com.hrznstudio.emojiful.ClientProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "emojiful")
    }
)
@Mixin(CommandSuggestions.class)
public class MixinCommandSuggestions {
    @WrapOperation(method = "showSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private int fixGiveCommandLag(Font instance, String pText, Operation<Integer> original){
        if(ClientProxy.oldFontRenderer != null) {
            return ClientProxy.oldFontRenderer.width(pText);
        }

        return original.call(instance, pText);
    }
}
