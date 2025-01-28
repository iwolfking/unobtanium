package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.init.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Long-standing bug in Vault Hunters: chests never had correct name. It became very silly with barrel introduction.
 * This mixin introduced proper chest names.
 */
@Mixin(value = VaultChestTileEntity.class)
public class FixVaultChestNames
{
    @Inject(method = "getDisplayName", at = @At(value = "TAIL"), cancellable = true)
    public void fixDisplayName(CallbackInfoReturnable<Component> cir)
    {
        BlockState state = ((VaultChestTileEntity) (Object) this).getBlockState();
        if (state.getBlock() == ModBlocks.WOODEN_CHEST || state.getBlock() == ModBlocks.WOODEN_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Wooden Chest"));
        }

        if (state.getBlock() == ModBlocks.GILDED_CHEST || state.getBlock() == ModBlocks.GILDED_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Gilded Chest"));
        }

        if (state.getBlock() == ModBlocks.LIVING_CHEST || state.getBlock() == ModBlocks.LIVING_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Living Chest"));
        }

        if (state.getBlock() == ModBlocks.ORNATE_CHEST || state.getBlock() == ModBlocks.ORNATE_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Ornate Chest"));
        }

        if (state.getBlock() == ModBlocks.TREASURE_CHEST || state.getBlock() == ModBlocks.TREASURE_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Treasure Chest"));
        }

        if (state.getBlock() == ModBlocks.ALTAR_CHEST || state.getBlock() == ModBlocks.ALTAR_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Altar Chest"));
        }

        if (state.getBlock() == ModBlocks.HARDENED_CHEST || state.getBlock() == ModBlocks.HARDENED_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Hardened Chest"));
        }

        if (state.getBlock() == ModBlocks.ENIGMA_CHEST || state.getBlock() == ModBlocks.ENIGMA_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Enigma Chest"));
        }

        if (state.getBlock() == ModBlocks.FLESH_CHEST || state.getBlock() == ModBlocks.FLESH_CHEST_PLACEABLE) {
            cir.setReturnValue(new TextComponent("Flesh Chest"));
        }

        if (state.getBlock() == ModBlocks.ORNATE_STRONGBOX) {
            cir.setReturnValue(new TextComponent("Ornate Strongbox"));
        }

        if (state.getBlock() == ModBlocks.GILDED_STRONGBOX) {
            cir.setReturnValue(new TextComponent("Gilded Strongbox"));
        }

        if (state.getBlock() == ModBlocks.LIVING_STRONGBOX) {
            cir.setReturnValue(new TextComponent("Living Strongbox"));
        }

        if (state.getBlock() == ModBlocks.WOODEN_BARREL) {
            cir.setReturnValue(new TextComponent("Wooden Barrel"));
        }

        if (state.getBlock() == ModBlocks.GILDED_BARREL) {
            cir.setReturnValue(new TextComponent("Gilded Barrel"));
        }

        if (state.getBlock() == ModBlocks.LIVING_BARREL) {
            cir.setReturnValue(new TextComponent("Living Barrel"));
        }

        if (state.getBlock() == ModBlocks.ORNATE_BARREL) {
            cir.setReturnValue(new TextComponent("Ornate Barrel"));
        }
    }
}
