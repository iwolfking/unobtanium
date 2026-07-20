package xyz.iwolfking.unobtainium.fixes;

import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.unobtainium.Unobtanium;
import xyz.iwolfking.unobtainium.api.helper.ReflectionHelper;
import xyz.iwolfking.unobtainium.mixin.ftblib.BaseScreenAccessor;

import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "unobtainium", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerStoppingMemoryLeakFix {
    public static final VarHandle BEACHED_CACHALOT_WHALE_SPAWNER_MAP;
    public static final VarHandle AM_DATA_MAP;
    public static final VarHandle SKY_MOB_SPAWNER_MAP;
    public static final VarHandle IMP_MUSIC_RINGS_MAP;
    public static final VarHandle LOOT_BEAMS_CACHE;
    static {
        AM_DATA_MAP = tryLoadMapField("com.github.alexthe666.alexsmobs.world.AMWorldData", "dataMap", true);
        BEACHED_CACHALOT_WHALE_SPAWNER_MAP = tryLoadMapField("com.github.alexthe666.alexsmobs.event.ServerEvents", "BEACHED_CACHALOT_WHALE_SPAWNER_MAP", true);
        SKY_MOB_SPAWNER_MAP = tryLoadMapField("com.github.alexthe668.cloudstorage.CommonProxy", "SKY_MOB_SPAWNER_MAP", true);
        IMP_MUSIC_RINGS_MAP = tryLoadMapField("dev.felnull.imp.server.music.ringer.MusicRingManager", "MUSIC_RINGS", false);
        LOOT_BEAMS_CACHE = tryLoadMapField("me.justahuman.vaultlootbeams.utils.Utils", "TOOLTIP_CACHE", true);
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
        removeLvlFromMap(AM_DATA_MAP, event.getWorld(), "AM_DATA_MAP");
        removeLvlFromMap(BEACHED_CACHALOT_WHALE_SPAWNER_MAP, event.getWorld(), "BEACHED_WHALE");
        removeLvlFromMap(SKY_MOB_SPAWNER_MAP, event.getWorld(), "SKY_MOB_SPAWNER");
        removeLvlFromImpMap(event.getWorld());
        clearEvCache();
        if (event.getWorld().isClientSide()) {
            if (ModList.get().isLoaded("ftblibrary")) {
                FTB.clear();
            }
        }
        if (LOOT_BEAMS_CACHE != null) {
            ((Map) LOOT_BEAMS_CACHE.get()).clear();
        }
    }

    @SubscribeEvent
    public void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
        if (event.getEntity() instanceof RemotePlayer player) {
            removePlayerFromPNC(player);
        }
    }

    private static void removeLvlFromMap(VarHandle handle, LevelAccessor level, String name) {
        try {
            if (handle != null) {
                Unobtanium.LOGGER.debug("Unloading level {} from {} map", level, name);
                ((Map) handle.get()).remove(level);
            }
        } catch (Exception e) {
            Unobtanium.LOGGER.warn("Failed to remove level from {} map: {}", name, e.getMessage());
        }
    }

    private static void removeLvlFromMap(VarHandle handle, LevelAccessor level, Object obj, String name) {
        try {
            if (handle != null) {
                Unobtanium.LOGGER.debug("Unloading level {} from {} map", level, name);
                ((Map) handle.get(obj)).remove(level);
            }
        } catch (Exception e) {
            Unobtanium.LOGGER.warn("Failed to remove level from {} map: {}", name, e.getMessage());
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
                removeLvlFromMap(IMP_MUSIC_RINGS_MAP, level, mgrInstance, "IMP");
            } catch (Exception e) {
                Unobtanium.LOGGER.warn("Failed to remove level from IMP map {}", e);
            }
        }
    }

    private static void clearEvCache() {
        if (!ModList.get().isLoaded("easy_villagers")) return;
        Object field = null;
        try {
            var fieldClass = Class.forName("de.maxhenkel.easy_villagers.corelib.CachedMap", false, ServerStoppingMemoryLeakFix.class.getClassLoader());
            Class<?> clazz = Class.forName("de.maxhenkel.easyvillagers.ItemTileEntityCache", false, ServerStoppingMemoryLeakFix.class.getClassLoader());
            field = ReflectionHelper.getFieldFromClass(clazz, "CACHE", fieldClass, true).get();
        } catch (Exception e) {
            Unobtanium.LOGGER.warn("Failed to remove level from EV map1 {}", e.getMessage());
        }
        if (field != null) {
            try {
                Class<?> mgrClass = Class.forName(
                    "de.maxhenkel.easy_villagers.corelib.CachedMap",
                    false,
                    ServerStoppingMemoryLeakFix.class.getClassLoader()
                );
                mgrClass.getMethod("clear").invoke(field);
            } catch (Exception e) {
                Unobtanium.LOGGER.warn("Failed to remove level from EV map2 {}", e.getMessage());
            }
        }
    }

    private static void removePlayerFromPNC(RemotePlayer player) {
        if (!ModList.get().isLoaded("pneumaticcraft")) return;
        try {
            var clazz = Class.forName("me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler", false, ServerStoppingMemoryLeakFix.class.getClassLoader());
            ReflectionHelper.getMethodFromClass(clazz, "clearHandlerForPlayer", MethodType.methodType(void.class, Player.class), true).invoke(player);
        } catch (Throwable e) {
            Unobtanium.LOGGER.warn("Failed to remove PNC player handler {}", e.getMessage());
        }
    }

    public static void clearPNCClientHandlers() {
        if (!ModList.get().isLoaded("pneumaticcraft")) return;
        try {
            var clazz = Class.forName("me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler", false, ServerStoppingMemoryLeakFix.class.getClassLoader());
            var clientHandler = ReflectionHelper.getFieldFromClass(clazz, "clientHandler", clazz, true);
            var playerHandler = ReflectionHelper.getFieldFromClass(clazz, "playerHandlers", HashMap.class, false);
            ((Map<UUID, CommonArmorHandler>)playerHandler.get((CommonArmorHandler)clientHandler.get())).clear();

        } catch (Throwable e) {
            Unobtanium.LOGGER.warn("Failed to clear PNC handlers {}", e.getMessage());
        }
    }

    private static class FTB {
        private static void clear() {
            if (GuiHelper.BLANK_GUI instanceof BaseScreenAccessor accessor) {
                accessor.setPrevScreen(null);
            }
        }
    }
}
