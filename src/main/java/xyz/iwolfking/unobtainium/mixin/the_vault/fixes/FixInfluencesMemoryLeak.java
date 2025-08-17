package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.Influences;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Influences.class, remap = false)
public class FixInfluencesMemoryLeak {
    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/AltarProgressEvent;in(Lnet/minecraft/world/level/Level;)Liskallia/vault/core/event/common/AltarProgressEvent;"), cancellable = true)
    private void removeLegacyCharmLogicEventRegistration(VirtualWorld world, Vault vault, Runner runner, CallbackInfo ci){
        ci.cancel();
    }
}
