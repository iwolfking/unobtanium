package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.init.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;


/**
 * U16 replaced `[type]_chest_placeable` with `[type]_chest`. This meant that both, player placed or vault generated,
 * chests had the same size in slot initialization. That size was also used for filling random slots with the generated
 * loot, and it meant that players could not see all loot inside chests, if they opened them.
 * This mixin fixes it, as it limits generated slot count to 27 (54 for treasure chests) and allow to fill only first
 * 27 (54 for treasure chests) slots.
 * DO NOT CHANGE VaultChestTileEntity#getSize as it will mess with player placed vault chests.
 */
@Mixin(value = VaultChestTileEntity.class)
public abstract class FixVaultChestGeneratedLootAmount
{
    @Shadow public abstract BlockState getBlockState();


    @Redirect(method = "fillLoot", at = @At(value = "FIELD", target = "Liskallia/vault/block/entity/VaultChestTileEntity;size:I", opcode = Opcodes.GETFIELD), remap = false)
    public int fixGeneratedLootSlots(VaultChestTileEntity instance)
    {
        // All generated vault chest inventories should be 27 (54 for treasure chest) items inside it.
        return instance.getBlockState().is(ModBlocks.TREASURE_CHEST) ? 54 : 27;
    }


    @Redirect(method = "getAvailableSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I"))
    public int fixFilledSlots(Container instance)
    {
        // All generated vault chest inventories should be 27 items inside it.
        return this.getBlockState().is(ModBlocks.TREASURE_CHEST) ? 54 : 27;
    }
}
