package xyz.iwolfking.unobtainium.sync;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AbilitySyncMessage {

    final byte[] body;

    public AbilitySyncMessage(byte[] body) {
        this.body = body;
    }

    public static void encode(AbilitySyncMessage msg, FriendlyByteBuf buf) {
        buf.writeByteArray(msg.body);
    }

    public static AbilitySyncMessage decode(FriendlyByteBuf buf) {
        return new AbilitySyncMessage(buf.readByteArray());
    }

    public static void handle(AbilitySyncMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> ClientAbilitySync.apply(msg.body));
        ctx.setPacketHandled(true);
    }
}
