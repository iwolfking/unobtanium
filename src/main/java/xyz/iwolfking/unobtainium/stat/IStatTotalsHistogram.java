package xyz.iwolfking.unobtainium.stat;

import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.util.VaultRarity;

public interface IStatTotalsHistogram {

    void unobtanium$recordLootedChest(VaultChestType type, VaultRarity rarity, int count);

    void unobtanium$recordTrappedChest(VaultChestType type, int count);
}
