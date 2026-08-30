package xyz.iwolfking.unobtainium.fixes;

import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.PartyStatusMessage;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.unobtainium.mixin.the_vault.VaultPartyDataAccessor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SyncVaultPartyData {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            VaultPartyData data = VaultPartyData.get(player.getLevel());
            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PartyStatusMessage(((VaultPartyDataAccessor)data).getActiveParties().serializeNBT()));
        }
    }

}
