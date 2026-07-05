package xyz.iwolfking.unobtainium.sync;

import io.netty.buffer.Unpooled;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.GenericFieldKey;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.data.sync.context.SyncContext;
import iskallia.vault.core.event.ClientEvents;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import xyz.iwolfking.unobtainium.Unobtanium;

public final class ClientFieldSync {

    private static final Map<Integer, long[]> CACHE = new HashMap<>();

    private ClientFieldSync() {
    }

    public static void apply(Version version, byte[] body) {
        Vault vault = ClientVaults.ACTIVE;
        ClientEvents.release(vault);
        CommonEvents.release(vault);

        KeyRegistry fields = vault.getFields();
        SyncContext ctx = new SyncContext(version);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(body));

        // A keyframe (sent on join/revive/reconnect) is a complete snapshot; drop any stale cache first so the
        // delta baseline is rebuilt exactly from it.
        if (buf.readBoolean()) {
            CACHE.clear();
        }

        int changed = buf.readVarInt();
        for (int c = 0; c < changed; c++) {
            int idx = buf.readVarInt();
            int before = buf.readableBytes();
            long[] data = LongArrayDelta.read(buf, CACHE.get(idx));
            int wire = before - buf.readableBytes();
            if (data == null) {
                Unobtanium.LOGGER.warn("[unobtanium] delta for field {} with no cached base; skipping", idx);
                continue;
            }
            CACHE.put(idx, data);

            GenericFieldKey key = (GenericFieldKey) fields.getKey(idx, version);
            Object value = key.readValue(version, ArrayBitBuffer.backing(data, 0), ctx);
            vault.set(key, value);
        }

        int removed = buf.readVarInt();
        for (int r = 0; r < removed; r++) {
            int idx = buf.readVarInt();
            GenericFieldKey key = (GenericFieldKey) fields.getKey(idx, version);
            vault.remove(key);
            CACHE.remove(idx);
        }

        vault.initClient();
    }
}
