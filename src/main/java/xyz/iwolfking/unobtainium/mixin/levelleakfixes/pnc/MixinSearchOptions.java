package xyz.iwolfking.unobtainium.mixin.levelleakfixes.pnc;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.gui.ItemSearcherScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.SearchOptions;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "pneumaticcraft")
    }
)
@Mixin(value = SearchOptions.class, remap = false)
public class MixinSearchOptions {
    @Shadow private static ItemSearcherScreen searchGui;

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", remap = true, opcode = Opcodes.GETFIELD))
    private LocalPlayer setPlayerToNull(Minecraft instance){
        return null;
    }

    @Inject(method = "populateGui", at = @At("TAIL"))
    private void clearGUI(IGuiScreen gui, CallbackInfo ci){
        searchGui = null;
    }

    @Redirect(method = {"populateGui", "openSearchGui"}, at = @At(value = "FIELD", target = "Lme/desht/pneumaticcraft/client/gui/pneumatic_armor/options/SearchOptions;player:Lnet/minecraft/world/entity/player/Player;"))
    private Player getFreshPlayer(SearchOptions instance){
        return ClientUtils.getClientPlayer();
    }

}
