package xyz.iwolfking.unobtainium.fixes;

import net.minecraft.world.level.LevelAccessor;
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
    public static final VarHandle SKY_MOB_SPAWNER_MAP;
    public static final VarHandle IMP_MUSIC_RINGS_MAP;
    static {
        AM_DATA_MAP = tryLoadMapField("com.github.alexthe666.alexsmobs.world.AMWorldData", "dataMap", true);
        BEACHED_CACHALOT_WHALE_SPAWNER_MAP = tryLoadMapField("com.github.alexthe666.alexsmobs.event.ServerEvents", "BEACHED_CACHALOT_WHALE_SPAWNER_MAP", true);
        SKY_MOB_SPAWNER_MAP = tryLoadMapField("com.github.alexthe668.cloudstorage.CommonProxy", "SKY_MOB_SPAWNER_MAP", true);
        IMP_MUSIC_RINGS_MAP = tryLoadMapField("dev.felnull.imp.server.music.ringer.MusicRingManager", "MUSIC_RINGS", false);
    }

    private static VarHandle tryLoadMapField(String className, String fieldName, boolean isStatic) {
        try {
            Class<?> clazz = Class.forName(className, false, ServerStoppingMemoryLeakFix.class.getClassLoader());
            return ReflectionHelper.getFieldFromClass(clazz, fieldName, Map.class, isStatic);
        } catch (Exception t) {
            Unobtanium.LOGGER.info("Optional dependency not found: {}", className);
            return null;
        }
    }

    @SubscribeEvent
    public static void LevelUnload(WorldEvent.Unload event) {
        removeLvlFromMap(AM_DATA_MAP, event.getWorld());
        removeLvlFromMap(BEACHED_CACHALOT_WHALE_SPAWNER_MAP, event.getWorld());
        removeLvlFromMap(SKY_MOB_SPAWNER_MAP, event.getWorld());
        removeLvlFromImpMap(event.getWorld());
    }

    private static void removeLvlFromMap(VarHandle handle, LevelAccessor level) {
        try {
            Unobtanium.LOGGER.info("Unloading level");
            ((Map) handle.get()).remove(level);
        } catch (Exception e) {
            Unobtanium.LOGGER.warn("Failed to remove level from map");
        }
    }

    private static void removeLvlFromMap(VarHandle handle, LevelAccessor level, Object obj) {
        try {
            Unobtanium.LOGGER.info("Unloading level");
            ((Map) handle.get(obj)).remove(level);
        } catch (Exception e) {
            Unobtanium.LOGGER.warn("Failed to remove level from map");
        }
    }

    private static void removeLvlFromImpMap(LevelAccessor level){
        if (IMP_MUSIC_RINGS_MAP != null) {
            try {
                Class<?> mgrClass = Class.forName(
                    "dev.felnull.imp.server.music.ringer.MusicRingManager",
                    false,
                    ServerStoppingMemoryLeakFix.class.getClassLoader()
                );
                Object mgrInstance = mgrClass.getMethod("getInstance").invoke(null);
                removeLvlFromMap(IMP_MUSIC_RINGS_MAP, level, mgrInstance);
            } catch (Exception ignored) {
                Unobtanium.LOGGER.warn("Failed to remove level from IMP map");
            }
        }
    }
}
