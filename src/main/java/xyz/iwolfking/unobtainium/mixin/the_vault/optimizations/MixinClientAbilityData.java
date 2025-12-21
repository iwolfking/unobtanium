package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.network.message.AbilityKnownOnesMessage;
import iskallia.vault.skill.base.TieredSkill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ClientAbilityData.class, remap = false)
public class MixinClientAbilityData {
    private static List<TieredSkill> cachedList = null;

    @Inject(method = "getLearnedAbilities", at = @At("HEAD"), cancellable = true)
    private static void useCached(CallbackInfoReturnable<List<TieredSkill>> cir){
        var cl = cachedList;
        if (cl != null) {
            cir.setReturnValue(cl);
        }
    }

    @Inject(method = "getLearnedAbilities", at = @At("TAIL"))
    private static void storeList(CallbackInfoReturnable<List<TieredSkill>> cir){
        cachedList = cir.getReturnValue();
    }

    @Inject(method = "updateAbilities", at = @At("TAIL"))
    private static void invalidateCache(AbilityKnownOnesMessage pkt, CallbackInfo ci){
        cachedList = null;
    }

}
