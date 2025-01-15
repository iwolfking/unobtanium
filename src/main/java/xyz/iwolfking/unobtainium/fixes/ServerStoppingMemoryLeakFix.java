package xyz.iwolfking.unobtainium.fixes;

import com.github.alexthe666.alexsmobs.event.ServerEvents;
import com.github.alexthe666.alexsmobs.world.AMWorldData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.unobtainium.Unobtanium;
import xyz.iwolfking.unobtainium.api.helper.ReflectionHelper;

import java.lang.invoke.VarHandle;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "unobtainium", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerStoppingMemoryLeakFix {
    public static final VarHandle BEACHED_CACHALOT_WHALE_SPAWNER_MAP;
    public static final VarHandle AM_DATA_MAP;
    static {
        AM_DATA_MAP = ReflectionHelper.getFieldFromClass(AMWorldData.class, "dataMap", Map.class, true);
        BEACHED_CACHALOT_WHALE_SPAWNER_MAP = ReflectionHelper.getFieldFromClass(ServerEvents.class, "BEACHED_CACHALOT_WHALE_SPAWNER_MAP", Map.class, true);
    }

    @SubscribeEvent
    public static void LevelUnload(WorldEvent.Unload event) {
            ((Map) AM_DATA_MAP.get()).remove(event.getWorld());
            ((Map) BEACHED_CACHALOT_WHALE_SPAWNER_MAP.get()).remove(event.getWorld());
    }
}
