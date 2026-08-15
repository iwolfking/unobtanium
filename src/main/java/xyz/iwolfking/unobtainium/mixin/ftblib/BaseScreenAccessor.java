package xyz.iwolfking.unobtainium.mixin.ftblib;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ftblibrary")
        }
)
@Mixin(value = BaseScreen.class, remap = false)
public interface BaseScreenAccessor {
    @Mutable @Accessor
    void setPrevScreen(Screen prevScreen);
}
