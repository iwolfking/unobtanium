package xyz.iwolfking.unobtainium.mixin.emojiful;

import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "emojiful")
        }
)
@Mixin(value = EmojiFontRenderer.class, remap = false)
public class MixinEmojiFontRenderer {
    @Redirect(method = {
            "width(Ljava/lang/String;)I",
            "width(Lnet/minecraft/network/chat/FormattedText;)I",
            "renderText",
            "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I"
    }, at = @At(value = "INVOKE", target = "Ljava/lang/String;replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private String noReplacement(String instance, String regex, String replacement){
        return instance;
    }
}
