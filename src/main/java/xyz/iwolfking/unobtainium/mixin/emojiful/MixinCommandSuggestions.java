package xyz.iwolfking.unobtainium.mixin.emojiful;

import com.hrznstudio.emojiful.ClientProxy;
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
    @Redirect(method = "showSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private int fixGiveCommandLag(Font instance, String pText){
        return ClientProxy.oldFontRenderer.width(pText);
    }
}
