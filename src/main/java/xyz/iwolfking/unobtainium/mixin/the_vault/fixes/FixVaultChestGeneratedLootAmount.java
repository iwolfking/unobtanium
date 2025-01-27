package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This mixin fixes how chests are filled. DO NOT CHANGE IT WITHOUT REASON.
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
