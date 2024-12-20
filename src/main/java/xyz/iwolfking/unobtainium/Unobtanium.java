package xyz.iwolfking.unobtainium;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;
import xyz.iwolfking.unobtainium.integration.VHAPIConfigRegistration;
import xyz.iwolfking.vhapi.api.events.VHAPIProcessorsEvent;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("unobtainium")
public class Unobtanium {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Unobtanium() {
        // Register the setup method for modloading
        if(LoadingModList.get().getModFileById("vhapi") != null) {
            MinecraftForge.EVENT_BUS.addListener(VHAPIConfigRegistration::onVHAPIProcessorEnd);
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation("unobtanium", path);
    }
}
