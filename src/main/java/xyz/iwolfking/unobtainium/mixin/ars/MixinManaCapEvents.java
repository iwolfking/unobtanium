package xyz.iwolfking.unobtainium.mixin.ars;

import com.hollingsworth.arsnouveau.common.event.ManaCapEvents;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ars_nouveau")
    }
)
@Mixin(value = ManaCapEvents.class, remap = false)
public class MixinManaCapEvents {
    /**
     * @author
     * @reason using wrong event
     */
    @Overwrite
    public static void playerLoggedIn(PlayerEvent.StartTracking e) {
    }
}
