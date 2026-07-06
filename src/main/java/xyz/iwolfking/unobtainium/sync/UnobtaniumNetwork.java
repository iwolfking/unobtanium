package xyz.iwolfking.unobtainium.sync;

import java.util.Optional;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.iwolfking.unobtainium.Unobtanium;

public final class UnobtaniumNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(Unobtanium.id("vault_sync"))
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .serverAcceptedVersions(PROTOCOL::equals)
        .networkProtocolVersion(() -> PROTOCOL)
        .simpleChannel();

    private UnobtaniumNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
            0,
            FieldSyncMessage.class,
            FieldSyncMessage::encode,
            FieldSyncMessage::decode,
            FieldSyncMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
            1,
            AbilitySyncMessage.class,
            AbilitySyncMessage::encode,
            AbilitySyncMessage::decode,
            AbilitySyncMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
