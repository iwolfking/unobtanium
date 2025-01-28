//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import iskallia.vault.core.vault.player.Listener;


/**
 * By default, vault timer starts when player is 15 blocks from start position. However, default room
 * from start point till exit is 15 blocks, which means that corners are outside 15 block range.
 * This mixin changes that, and adds extra 5 blocks that should be enough for all impatient players
 * that cannot sit on spot while waiting for other players.
 */
@Mixin(value = Listener.class, remap = false)
public class FixVaultTimerStarting
{
    @ModifyConstant(method = "lambda$tickServer$0", constant = @Constant(doubleValue = 225.0f))
    private static double fixRoomSize(double constant)
    {
        // Add extra 5 blocks before timer starts.
        return 400;
    }
}
