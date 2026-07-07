package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.init.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// coalesce a vault ore's drops (copiously procs) into as few stacks as possible
// also swallows the per-proc sound, and instead sends one sound per block scaling with proc amount
@Mixin(VaultOreBlock.class)
public abstract class CopiousOreDropCoalesceMixin {

    private static final ThreadLocal<Integer> unobtainium$copiousProcs = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "getDrops", at = @At("HEAD"))
    private void unobtainium$resetCopiousSound(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        unobtainium$copiousProcs.set(0);
    }

    @Redirect(
        method = "getDrops",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        )
    )
    private void unobtainium$swallowCopiousSound(Level level, Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        unobtainium$copiousProcs.set(unobtainium$copiousProcs.get() + 1);
    }

    @Inject(method = "getDrops", at = @At("RETURN"))
    private void unobtainium$coalesceDrops(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        int procs = unobtainium$copiousProcs.get();
        if (procs > 0 && builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer sPlayer) {
            // chime once for this block's whole copiously burst, scaling in volume a bit.
            BlockPos at = new BlockPos(sPlayer.getBlockX(), sPlayer.getBlockY(), sPlayer.getBlockZ());
            float volume = (float) Math.min(1.0, 0.1 * (1.0 + Math.log(procs)));
            sPlayer.getLevel().playSound(null, at, ModSounds.VAULT_CHEST_OMEGA_OPEN, SoundSource.BLOCKS, volume, 0.85F);
        }

        List<ItemStack> drops = cir.getReturnValue();
        if (drops == null || drops.size() < 2) {
            return;
        }

        boolean merged = false;
        for (int i = 0; i < drops.size(); i++) {
            ItemStack base = drops.get(i);
            if (base.isEmpty()) {
                continue;
            }
            int max = base.getMaxStackSize();
            if (max <= 1) {
                continue;
            }
            for (int j = i + 1; j < drops.size() && base.getCount() < max; j++) {
                ItemStack other = drops.get(j);
                if (other.isEmpty() || !ItemStack.isSame(base, other) || !ItemStack.tagMatches(base, other)) {
                    continue;
                }
                int move = Math.min(other.getCount(), max - base.getCount());
                base.grow(move);
                other.shrink(move);
                merged = true;
            }
        }

        if (merged) {
            drops.removeIf(ItemStack::isEmpty);
        }
    }
}
