package xyz.iwolfking.unobtainium.sync;

import iskallia.vault.core.net.ArrayBitBuffer;
import java.util.Arrays;

public final class ReusableBitBuffer extends ArrayBitBuffer {

    public ReusableBitBuffer() {
        super(new long[64], 0);
    }

    public int usedLongs() {
        return (this.position + 63) >>> 6;
    }

    public long[] backing() {
        return this.buffer;
    }

    public void reset() {
        int used = usedLongs();
        if (used > 0) {
            Arrays.fill(this.buffer, 0, Math.min(used, this.buffer.length), 0L);
        }
        this.position = 0;
    }
}
