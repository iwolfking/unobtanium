package xyz.iwolfking.unobtainium.mixin.inventoryhud;

import dlovin.inventoryhud.armorstatus.ArmorStatus;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;


@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "inventoryhud")
    }
)
@Mixin(value = ArmorStatus.class, remap = false)
public class MixinArmorStatus {

    @Unique private static final Map<ItemStack, Integer> unobtainium$dmgCache = new HashMap<>();
    @Unique private static int unobtainium$lastTick = 0;

    @Inject(method = "getDamage", at = @At("HEAD"), cancellable = true)
    private static void getFromCache(ItemStack item, CallbackInfoReturnable<Integer> cir){
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (unobtainium$lastTick == player.tickCount) {
            var dmg = unobtainium$dmgCache.get(item);
            if (dmg != null) {
                cir.setReturnValue(dmg);
            }
        }
    }

    @Inject(method = "getDamage", at = @At("RETURN"))
    private static void storeToCache(ItemStack item, CallbackInfoReturnable<Integer> cir){
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (unobtainium$lastTick != player.tickCount) {
            unobtainium$dmgCache.clear();
            unobtainium$lastTick = player.tickCount;
        }
        unobtainium$dmgCache.put(item, cir.getReturnValue());
    }
}
