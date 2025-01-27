//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import xyz.iwolfking.unobtainium.optimizations.ActiveCardTaskHelper;


/**
 * ActiveCardTaskHelper is a wrapper just to make Vaults class mixin smaller.
 */
@Mixin(value = Vault.class, remap = false)
public class InjectCardProcessorVault
{
    @Inject(method = "initServer", at = @At("TAIL"))
    public void injectNewTasks(VirtualWorld world, CallbackInfo ci)
    {
        new ActiveCardTaskHelper().initServer(world, (Vault) (Object) this);
    }
}
