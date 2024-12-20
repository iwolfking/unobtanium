package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import iskallia.vault.skill.talent.type.StackingGearAttributeTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = StackingGearAttributeTalent.class, remap = false, priority = 1500)
public class OptimizeGearSnapshots extends GearAttributeTalent {
    @Shadow
    private int timeLeft;
    @Shadow private int stacks;
    @Shadow private MobEffect effect;

    /**
     * @author iwolfking
     * @reason Due to how these kinds of talents are implemented in vanilla Vault Hunters, they cause gear snapshots to constantly get recalculated for every player every tick
     * this fixes the issue completely: those without the talents unlocked will never refresh snapshot due to this bug and when there are no stacks it also will not update your stats.
     * This saves about 4-5% of server tick time when player count is high.
     */
    @Overwrite
    public void onTick(SkillContext context) {
        if (--this.timeLeft < 0 && this.isUnlocked() && this.stacks > 0) {
            this.timeLeft = 0;
            this.stacks = 0;
            context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
        }

        super.onTick(context);
        if (this.isUnlocked() && this.effect != null && this.timeLeft > 0 && this.stacks > 0) {
            context.getSource().as(ServerPlayer.class).ifPresent((player) -> {
                player.removeEffect(this.effect);
                player.addEffect(new MobEffectInstance(this.effect, this.timeLeft, this.stacks - 1, true, false, true));
            });
        }

    }
}
