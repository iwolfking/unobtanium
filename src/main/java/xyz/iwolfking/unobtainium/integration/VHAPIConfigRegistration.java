package xyz.iwolfking.unobtainium.integration;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.unobtainium.Unobtanium;
import xyz.iwolfking.vhapi.VHAPI;
import xyz.iwolfking.vhapi.api.events.VHAPIProcessorsEvent;
import xyz.iwolfking.vhapi.api.loaders.objectives.ScavengerConfigLoader;
import xyz.iwolfking.vhapi.api.loaders.vault.mobs.VaultMobsConfigLoader;
import xyz.iwolfking.vhapi.api.loaders.vault.modifiers.VaultModifierPoolsConfigLoader;
import xyz.iwolfking.vhapi.api.loaders.workstation.VaultRecyclerConfigLoader;
import xyz.iwolfking.vhapi.api.util.VHAPIProcesserUtils;
import xyz.iwolfking.vhapi.api.util.vhapi.VHAPILoggerUtils;

import java.io.IOException;
import java.io.InputStream;

public class VHAPIConfigRegistration {

    public static void addManualConfigs() {
        Unobtanium.LOGGER.info("Registering Vault config patches!");
        registerManualConfigFile("/config_patches/guardian_thorns_patch.json", Unobtanium.id("vault/mobs/guardian_thorns_patch"));
        registerManualConfigFile("/config_patches/overgrown_tank_reach.json", Unobtanium.id("vault/mobs/overgrown_tank_reach_patch"));
        registerManualConfigFile("/config_patches/vault_dweller_speed_fix.json", Unobtanium.id("vault/mobs/vault_dweller_speed_patch"));
        registerManualConfigFile("/config_patches/more_mobs_brazier_fix.json", Unobtanium.id("vault/modifier_pools/more_mobs_brazier_patch"));
        registerManualConfigFile("/config_patches/scavenger_missing_mobs_fix.json", Unobtanium.id("objectives/scavenger/missing_mobs_patch"));
        registerManualConfigFile("/config_patches/gemstone_recycle_rate_patch.json", Unobtanium.id("vault_recycler/gemstone_recycle_patch"));
    }


    public static void registerManualConfigFile(String filePath, ResourceLocation vhapiRegistryId) {
        try (InputStream stream = Unobtanium.class.getResourceAsStream(filePath)) {
            if (stream == null) {
                throw new IOException();
            }
            VHAPIProcesserUtils.addManualConfigFile(stream, vhapiRegistryId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onVHAPIProcessorEnd(VHAPIProcessorsEvent.End event) {
        addManualConfigs();
    }
}
