//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import iskallia.vault.core.card.ActiveCardTaskHelper;
import net.minecraftforge.event.TickEvent;


/**
 * The best optimization is to remove execution every tick. This will actually completely remove
 * any processing that is done in this class.
 */
@Mixin(value = ActiveCardTaskHelper.class, remap = false)
public class OptimizeActiveTaskHelper
{
    @Inject(method = "onServerTick", at = @At("HEAD"), cancellable = true)
    private static void cancelServerTick(TickEvent.ServerTickEvent event, CallbackInfo ci)
    {
        // Completely disable the active card task helper processor
        ci.cancel();
    }
}
