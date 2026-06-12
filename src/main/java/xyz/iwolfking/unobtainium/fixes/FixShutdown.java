package xyz.iwolfking.unobtainium.fixes;

import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.unobtainium.mixin.the_vault.VirtualWorldsAccessor;

@Mod.EventBusSubscriber(modid = "unobtainium")
public class FixShutdown {
    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event) {
        VirtualWorldsAccessor.getCONCURRENT_POOL().shutdown();
    }
}
