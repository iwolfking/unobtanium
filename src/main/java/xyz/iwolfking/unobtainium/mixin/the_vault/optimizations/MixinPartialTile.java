package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.core.world.data.tile.PartialTile;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.iwolfking.unobtainium.fixes.VaultPosReuse;

@Mixin(value = PartialTile.class, remap = false)
public class MixinPartialTile {
    @ModifyArg(method = {"of*"}, at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/data/tile/PartialTile;<init>(Liskallia/vault/core/world/data/tile/PartialBlockState;Liskallia/vault/core/world/data/entity/PartialCompoundNbt;Lnet/minecraft/core/BlockPos;)V"))
    private static BlockPos reusePos(BlockPos pos) {
        return VaultPosReuse.sharedPos(pos);
    }
}
