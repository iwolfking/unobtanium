package xyz.iwolfking.unobtainium.fixes;

import iskallia.vault.core.world.threading.ThreadPool;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
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

    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event) {
        if (VirtualWorldsAccessor.getCONCURRENT_POOL().isShutdown()) {
            VirtualWorldsAccessor.setCONCURRENT_POOL(new ThreadPool(Runtime.getRuntime().availableProcessors()));
        }
    }
}
