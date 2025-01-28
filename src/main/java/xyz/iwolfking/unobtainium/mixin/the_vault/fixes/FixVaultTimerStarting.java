//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.core.vault.player.Listener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;


/**
 * By default, vault timer starts when player is 15 blocks from start position. However, default room
 * from start point till exit is 15 blocks, which means that corners are outside 15 block range.
 * This mixin changes that, and adds extra 5 blocks that should be enough for all impatient players
 * that cannot sit on spot while waiting for other players.
 */
@Mixin(value = Listener.class)
public class FixVaultTimerStarting
{
    @ModifyConstant(method = "lambda$tickServer$0", constant = @Constant(doubleValue = 225.0f), remap = false)
    private static double fixRoomSize(double constant)
    {
        // Add extra 5 blocks before timer starts.
        return 400;
    }


    @Redirect(method = "lambda$tickServer$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private static double fixRoomSize(ServerPlayer player, Vec3 vec3)
    {
        // calculate by ignoring Y, as vertical distance in current situation does not matter.
        double x = player.getX() - vec3.x();
        double z = player.getZ() - vec3.z();

        return x * x + z * z;
    }
}
