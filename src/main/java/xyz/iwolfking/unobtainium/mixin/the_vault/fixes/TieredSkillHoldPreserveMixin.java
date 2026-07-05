package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.HoldAbility;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// capture held state before regretting current-tier leaf
// and re-apply held state after re-learning the new current-tier leaf
@Mixin(value = TieredSkill.class, remap = false)
public class TieredSkillHoldPreserveMixin {

    @Unique
    private boolean unobtainium$heldActiveBeforeShift;

    @Inject(method = "updateBonusTier(ILiskallia/vault/skill/base/SkillContext;)V", at = @At("HEAD"), remap = false)
    private void unobtainium$captureHold(int bonusTier, SkillContext context, CallbackInfo ci) {
        LearnableSkill child = ((TieredSkill) (Object) this).getChild();
        unobtainium$heldActiveBeforeShift = child instanceof HoldAbility hold && hold.isActive();
    }

    @Inject(method = "updateBonusTier(ILiskallia/vault/skill/base/SkillContext;)V", at = @At("TAIL"), remap = false)
    private void unobtainium$restoreHold(int bonusTier, SkillContext context, CallbackInfo ci) {
        if (!unobtainium$heldActiveBeforeShift) {
            return;
        }
        LearnableSkill child = ((TieredSkill) (Object) this).getChild();
        if (child instanceof Ability ability && ability.isUnlocked() && !ability.isActive()) {
            ability.setActive(true);
        }
    }
}
