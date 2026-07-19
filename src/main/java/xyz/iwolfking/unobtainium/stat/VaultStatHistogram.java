package xyz.iwolfking.unobtainium.stat;

import iskallia.vault.core.data.DataMap;
import iskallia.vault.core.data.adapter.Adapters;
import java.util.HashMap;

public class VaultStatHistogram extends DataMap<VaultStatHistogram, Integer, Integer> {

    public VaultStatHistogram() {
        super(new HashMap<>(), Adapters.INT_SEGMENTED_7, Adapters.INT_SEGMENTED_7);
    }

    public void increment(int key) {
        this.put(key, this.getOrDefault(key, 0) + 1);
    }
}
