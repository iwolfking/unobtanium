package xyz.iwolfking.unobtainium.sync;

import net.minecraft.network.FriendlyByteBuf;

public final class LongArrayDelta {

    private LongArrayDelta() {
    }

    public static void write(FriendlyByteBuf buf, long[] current, long[] cached) {
        if (cached != null && cached.length == current.length) {
            int diffCount = 0;
            for (int i = 0; i < current.length; i++) {
                if (current[i] != cached[i]) {
                    diffCount++;
                }
            }

            buf.writeBoolean(true);
            buf.writeVarInt(diffCount);
            for (int i = 0; i < current.length; i++) {
                if (current[i] != cached[i]) {
                    buf.writeVarInt(i);
                    buf.writeLong(current[i]);
                }
            }
            return;
        }
        buf.writeBoolean(false);
        buf.writeLongArray(current);
    }

    public static long[] read(FriendlyByteBuf buf, long[] base) {
        boolean isDelta = buf.readBoolean();
        if (!isDelta) {
            return buf.readLongArray();
        }
        int count = buf.readVarInt();
        if (base == null) {
            for (int i = 0; i < count; i++) {
                buf.readVarInt();
                buf.readLong();
            }
            return null;
        }
        for (int i = 0; i < count; i++) {
            base[buf.readVarInt()] = buf.readLong();
        }
        return base;
    }
}
