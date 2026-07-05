package xyz.iwolfking.unobtainium.sync;

import iskallia.vault.core.Version;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class FieldSyncMessage {

    final Version version;
    final byte[] body;

    public FieldSyncMessage(Version version, byte[] body) {
        this.version = version;
        this.body = body;
    }

    public static void encode(FieldSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.version);
        buf.writeByteArray(msg.body);
    }

    public static FieldSyncMessage decode(FriendlyByteBuf buf) {
        return new FieldSyncMessage(buf.readEnum(Version.class), buf.readByteArray());
    }

    public static void handle(FieldSyncMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientFieldSync.apply(msg.version, msg.body));
        ctx.setPacketHandled(true);
    }
}
