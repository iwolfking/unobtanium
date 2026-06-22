package xyz.iwolfking.unobtainium.fixes;

import com.hollingsworth.arsnouveau.common.event.ManaCapEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "unobtainium")
public class FixArsManaEventPerf {
    private static final boolean AN_LOADED = ModList.get().isLoaded("ars_nouveau");
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (AN_LOADED) ArsClass.invokeSyncEvent(event.getPlayer());
    }

    private static class ArsClass {
        public static void invokeSyncEvent(Player player){
            ManaCapEvents.syncPlayerEvent(player);
        }
    }
}
