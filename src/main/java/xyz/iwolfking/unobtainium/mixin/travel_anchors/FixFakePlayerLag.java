package xyz.iwolfking.unobtainium.mixin.travel_anchors;


import de.castcrafter.travel_anchors.EventListener;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "travel_anchors")
        }
)
@Mixin(value = EventListener.class, remap = false)
public class FixFakePlayerLag {
    @Inject(method = "onRightClick", at = @At("HEAD"), cancellable = true)
    private void fakePlayerPass(PlayerInteractEvent.RightClickItem event, CallbackInfo ci) {
        if(event.getEntity() instanceof FakePlayer) {
            ci.cancel();
        }
    }
}
