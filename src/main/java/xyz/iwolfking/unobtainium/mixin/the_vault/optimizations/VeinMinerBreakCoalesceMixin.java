package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.skill.ability.effect.spi.AbstractVeinMinerAbility;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.drops.DropCoalescer;

@Mixin(value = AbstractVeinMinerAbility.class, remap = false)
public abstract class VeinMinerBreakCoalesceMixin {

    @Inject(method = "onBlockMined", at = @At("HEAD"), remap = false)
    private static void unobtainium$beginCoalesce(BlockEvent.BreakEvent event, CallbackInfo ci) {
        DropCoalescer.begin(event.getPlayer().level, event.getPlayer());
    }

    @Inject(method = "onBlockMined", at = @At("RETURN"), remap = false)
    private static void unobtainium$endCoalesce(BlockEvent.BreakEvent event, CallbackInfo ci) {
        DropCoalescer.end();
    }
}
