package xyz.iwolfking.unobtainium.stat;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.ICompound;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.unobtainium.Unobtanium;


/* 
 * StatCollector indices are recomputed against the live registry on every read,
 * so the new fields MUST sort to the very end of the list otherwise we shift the_vault's own indices,
 * and every serialized vault reads its fields through the wrong adapters
*/
public final class VaultStatFields {
    
    /** Chest histogram: packed key {@code ((type*5 + rarityCode) << 1) | trapped} -> count.
     *  path is zzz_-prefixed so it sorts last */
    public static final FieldKey<VaultStatHistogram> CHEST_STATS =
        FieldKey.of(new ResourceLocation("unobtanium", "zzz_chest_stats"), VaultStatHistogram.class)
            .with(Version.v1_65, CompoundAdapter.of(VaultStatHistogram::new), ICompound.DISK.all().or(ICompound.CLIENT.all()));

    /** Barrel histogram: packed key {@code type*5 + rarityCode} -> count.
     *  path is zzz_-prefixed so it sorts last */
    public static final FieldKey<VaultStatHistogram> BARREL_STATS =
        FieldKey.of(new ResourceLocation("unobtanium", "zzz_barrel_stats"), VaultStatHistogram.class)
            .with(Version.v1_65, CompoundAdapter.of(VaultStatHistogram::new), ICompound.DISK.all().or(ICompound.CLIENT.all()));

    private static boolean registered = false;

    private VaultStatFields() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHEST_STATS.register(StatCollector.FIELDS);
        BARREL_STATS.register(StatCollector.FIELDS);
        verifySortedLast();
    }

    private static void verifySortedLast() {
        Version v = Version.latest();
        int size = StatCollector.FIELDS.getSize(v);
        int chestIdx = StatCollector.FIELDS.getIndex(CHEST_STATS.getId(), v);
        int barrelIdx = StatCollector.FIELDS.getIndex(BARREL_STATS.getId(), v);
        // both fields must land in the top two indices
        if (chestIdx < size - 2 || barrelIdx < size - 2) {
            Unobtanium.LOGGER.error(
                "VaultStatFields are NOT sorted last in StatCollector.FIELDS (size={}, chest={}, barrel={}). "
                    + "A field now sorts after them, so existing vault stats will misread.",
                size, chestIdx, barrelIdx);
        }
    }

    // key packing
    // rarityCode: 0 = no rarity (null), else rarity.ordinal() + 1. VaultRarity has 4 values -> code in 0..4.

    public static int chestKey(VaultChestType type, VaultRarity rarity, boolean trapped) {
        int rarityCode = rarity == null ? 0 : rarity.ordinal() + 1;
        return ((type.ordinal() * 5 + rarityCode) << 1) | (trapped ? 1 : 0);
    }

    public static VaultChestType chestType(int key) {
        return VaultChestType.values()[(key >> 1) / 5];
    }

    public static VaultRarity chestRarity(int key) {
        int rarityCode = (key >> 1) % 5;
        return rarityCode == 0 ? null : VaultRarity.values()[rarityCode - 1];
    }

    public static boolean chestTrapped(int key) {
        return (key & 1) != 0;
    }

    public static int barrelKey(VaultChestType type, VaultRarity rarity) {
        int rarityCode = rarity == null ? 0 : rarity.ordinal() + 1;
        return type.ordinal() * 5 + rarityCode;
    }

    public static VaultChestType barrelType(int key) {
        return VaultChestType.values()[key / 5];
    }

    public static VaultRarity barrelRarity(int key) {
        int rarityCode = key % 5;
        return rarityCode == 0 ? null : VaultRarity.values()[rarityCode - 1];
    }

    // population

    public static void recordChest(StatCollector self, VaultChestType type, VaultRarity rarity, boolean trapped) {
        ensure(self, CHEST_STATS).increment(chestKey(type, rarity, trapped));
    }

    public static void recordBarrel(StatCollector self, VaultChestType type, VaultRarity rarity) {
        ensure(self, BARREL_STATS).increment(barrelKey(type, rarity));
    }

    private static VaultStatHistogram ensure(StatCollector self, FieldKey<VaultStatHistogram> key) {
        if (!self.has(key)) {
            self.set(key, new VaultStatHistogram());
        }
        return self.get(key);
    }

    // reads

    public static VaultStatHistogram chests(StatCollector self) {
        return self.has(CHEST_STATS) ? self.get(CHEST_STATS) : null;
    }

    public static VaultStatHistogram barrels(StatCollector self) {
        return self.has(BARREL_STATS) ? self.get(BARREL_STATS) : null;
    }

    public static int chestCount(StatCollector self, VaultChestType type, VaultRarity rarity, boolean trapped) {
        VaultStatHistogram h = chests(self);
        return h == null ? 0 : h.getOrDefault(chestKey(type, rarity, trapped), 0);
    }

    public static int trappedCount(StatCollector self, VaultChestType type) {
        return chestCount(self, type, null, true);
    }

    public static int barrelCount(StatCollector self, VaultChestType type, VaultRarity rarity) {
        VaultStatHistogram h = barrels(self);
        return h == null ? 0 : h.getOrDefault(barrelKey(type, rarity), 0);
    }

    public static void forEachLootedChest(StatCollector self, ChestConsumer consumer) {
        VaultStatHistogram h = chests(self);
        if (h == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> e : h.entrySet()) {
            int key = e.getKey();
            if (!chestTrapped(key)) {
                consumer.accept(chestType(key), chestRarity(key), e.getValue());
            }
        }
    }

    public static void forEachTrappedChest(StatCollector self, TrappedConsumer consumer) {
        VaultStatHistogram h = chests(self);
        if (h == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> e : h.entrySet()) {
            int key = e.getKey();
            if (chestTrapped(key)) {
                consumer.accept(chestType(key), e.getValue());
            }
        }
    }

    public static void forEachLootedBarrel(StatCollector self, ChestConsumer consumer) {
        VaultStatHistogram h = barrels(self);
        if (h == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> e : h.entrySet()) {
            int key = e.getKey();
            consumer.accept(barrelType(key), barrelRarity(key), e.getValue());
        }
    }

    // (type, rarity, count) visitor, shared by chest and barrel iteration
    @FunctionalInterface
    public interface ChestConsumer {
        void accept(VaultChestType type, VaultRarity rarity, int count);
    }

    // (type, count) visitor for trapped chests (no rarity)
    @FunctionalInterface
    public interface TrappedConsumer {
        void accept(VaultChestType type, int count);
    }
}
