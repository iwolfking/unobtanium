package xyz.iwolfking.unobtainium.mixin.the_vault;

import iskallia.vault.nbt.VListNBT;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VaultPartyData.class, remap = false)
public interface VaultPartyDataAccessor {
    @Accessor
    VListNBT<VaultPartyData.Party, CompoundTag> getActiveParties();
}
