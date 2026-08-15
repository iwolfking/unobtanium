package xyz.iwolfking.unobtainium.mixin.levelleakfixes.justenoughcalculation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.towdium.jecalculation.events.GuiScreenOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jecalculation")
        }
)
@Mixin(value = GuiScreenOverlayHandler.class)
public class MixinGuiScreenOverlayHandler {

    @WrapOperation(method = "inventoryToWidgets", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;items:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.GETFIELD))
    private NonNullList<ItemStack> getFreshPlayer(Inventory instance, Operation<NonNullList<ItemStack>> original){
        if (instance != null) {
            return original.call(instance);
        }
        var player = Minecraft.getInstance().player;
        if (player == null) return NonNullList.create();
        return player.getInventory().items;
    }
}
