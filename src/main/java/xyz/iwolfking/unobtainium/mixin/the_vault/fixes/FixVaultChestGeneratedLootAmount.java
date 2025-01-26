package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import iskallia.vault.init.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import iskallia.vault.block.entity.VaultChestTileEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = VaultChestTileEntity.class, remap = false)
public class FixVaultChestGeneratedLootAmount {
    @Inject(method = "getSize", at = @At("HEAD"), cancellable = true)
    private void getSize(BlockState state, CallbackInfoReturnable<Integer> cir) {
        Block block = state.getBlock();
        if (block == ModBlocks.WOODEN_CHEST
            || block == ModBlocks.LIVING_CHEST
            || block == ModBlocks.ORNATE_CHEST
            || block == ModBlocks.GILDED_CHEST
            || block == ModBlocks.FLESH_CHEST
            || block == ModBlocks.HARDENED_CHEST
            || block == ModBlocks.ENIGMA_CHEST
        ) {
            cir.setReturnValue(27);
        }
    }
}
