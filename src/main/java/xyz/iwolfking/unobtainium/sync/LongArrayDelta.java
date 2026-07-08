package xyz.iwolfking.unobtainium.sync;

import net.minecraft.network.FriendlyByteBuf;

/**
 * encodes one field's serialized long[] against the previously-synced value, picking the smallest of three modes:
 *
 * MODE_FULL - the whole value (used when there is no base, or nothing else is cheaper).
 * MODE_SPARSE - same length as the base: a list of (index, value) for only the slots that differ
 * MODE_PREFIX - a shared leading prefix plus a literal tail for append-only growth
 */
public final class LongArrayDelta {

    private static final int MODE_FULL = 0;
    private static final int MODE_SPARSE = 1;
    private static final int MODE_PREFIX = 2;

    private LongArrayDelta() {
    }

    // convenience for exact-length arrays
    public static void write(FriendlyByteBuf buf, long[] current, long[] cached) {
        write(buf, current, current.length, cached, cached == null ? 0 : cached.length);
    }

    public static void write(FriendlyByteBuf buf, long[] current, int curLen, long[] cached, int cacheLen) {
        if (cached == null) {
            writeFull(buf, current, curLen);
            return;
        }

        int max = Math.min(curLen, cacheLen);
        int prefix = 0;
        while (prefix < max && current[prefix] == cached[prefix]) {
            prefix++;
        }

        int tail = curLen - prefix;
        long prefixCost = 1L + varLen(prefix) + varLen(tail) + (long) tail * 8L;
        long fullCost = 1L + varLen(curLen) + (long) curLen * 8L;

        if (curLen == cacheLen) {
            int diffs = 0;
            for (int i = prefix; i < curLen; i++) {
                if (current[i] != cached[i]) {
                    diffs++;
                }
            }
            long sparseCost = 1L + varLen(diffs) + (long) diffs * (5L + 8L);
            if (sparseCost <= prefixCost && sparseCost <= fullCost) {
                writeSparse(buf, current, cached, curLen, diffs);
                return;
            }
        }

        if (prefixCost < fullCost) {
            writePrefix(buf, current, curLen, prefix);
        } else {
            writeFull(buf, current, curLen);
        }
    }

    public static long[] read(FriendlyByteBuf buf, long[] base) {
        int mode = buf.readByte();
        switch (mode) {
            case MODE_FULL: {
                int len = buf.readVarInt();
                long[] result = new long[len];
                for (int i = 0; i < len; i++) {
                    result[i] = buf.readLong();
                }
                return result;
            }
            case MODE_SPARSE: {
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
            case MODE_PREFIX: {
                int prefix = buf.readVarInt();
                int tail = buf.readVarInt();
                if (base == null) {
                    for (int i = 0; i < tail; i++) {
                        buf.readLong();
                    }
                    return null;
                }
                long[] result = new long[prefix + tail];
                System.arraycopy(base, 0, result, 0, Math.min(prefix, base.length));
                for (int i = 0; i < tail; i++) {
                    result[prefix + i] = buf.readLong();
                }
                return result;
            }
            default:
                throw new IllegalStateException("Unknown LongArrayDelta mode " + mode);
        }
    }

    private static void writeFull(FriendlyByteBuf buf, long[] current, int curLen) {
        buf.writeByte(MODE_FULL);
        buf.writeVarInt(curLen);
        for (int i = 0; i < curLen; i++) {
            buf.writeLong(current[i]);
        }
    }

    private static void writeSparse(FriendlyByteBuf buf, long[] current, long[] cached, int curLen, int diffCount) {
        buf.writeByte(MODE_SPARSE);
        buf.writeVarInt(diffCount);
        for (int i = 0; i < curLen; i++) {
            if (current[i] != cached[i]) {
                buf.writeVarInt(i);
                buf.writeLong(current[i]);
            }
        }
    }

    private static void writePrefix(FriendlyByteBuf buf, long[] current, int curLen, int prefix) {
        buf.writeByte(MODE_PREFIX);
        buf.writeVarInt(prefix);
        buf.writeVarInt(curLen - prefix);
        for (int i = prefix; i < curLen; i++) {
            buf.writeLong(current[i]);
        }
    }

    private static int varLen(int value) {
        int n = 1;
        while ((value & ~0x7F) != 0) {
            value >>>= 7;
            n++;
        }
        return n;
    }
}
