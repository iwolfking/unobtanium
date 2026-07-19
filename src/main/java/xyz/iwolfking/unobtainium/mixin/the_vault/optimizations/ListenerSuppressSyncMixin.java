package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// cancelling the per-tick vault data syncing
@Mixin(value = Listener.class, remap = false)
public class ListenerSuppressSyncMixin {

    @Inject(
        method = "lambda$tickServer$2(Liskallia/vault/core/vault/Vault;Liskallia/vault/core/world/storage/VirtualWorld;Lnet/minecraft/server/level/ServerPlayer;)V",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETSTATIC,
            target = "Liskallia/vault/init/ModNetwork;CHANNEL:Lnet/minecraftforge/network/simple/SimpleChannel;",
            remap = false
        ),
        remap = false,
        cancellable = true
    )
    private static void unobtainium$suppressPerTickFullSync(Vault vault, VirtualWorld world, ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
