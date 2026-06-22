package xyz.iwolfking.unobtainium.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(dev.ftb.mods.ftblibrary.ui.BaseScreen.class)
public interface BaseScreenAccessor {


    @Mutable @Accessor
    void setPrevScreen(Screen prevScreen);
}
