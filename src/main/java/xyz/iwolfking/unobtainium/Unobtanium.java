package xyz.iwolfking.unobtainium;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;
import xyz.iwolfking.unobtainium.api.lib.tooltip.ContainerItemTooltip;
import xyz.iwolfking.unobtainium.client.tooltip.ClientContainerItemTooltip;
import xyz.iwolfking.unobtainium.integration.VHAPIConfigRegistration;

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

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(UnobtaniumClient::onClientSetup);
        });

        MinecraftForge.EVENT_BUS.register(this);
    }


    public static ResourceLocation id(String path) {
        return new ResourceLocation("unobtanium", path);
    }

    @Mod.EventBusSubscriber(modid = "unobtanium", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class UnobtaniumClient {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent evt) {
            MinecraftForgeClient.registerTooltipComponentFactory(ContainerItemTooltip.class, ClientContainerItemTooltip::new);
        }
    }
}
