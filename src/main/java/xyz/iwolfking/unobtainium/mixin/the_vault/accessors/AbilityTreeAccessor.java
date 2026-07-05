package xyz.iwolfking.unobtainium.mixin.the_vault.accessors;

import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbilityTree.class, remap = false)
public interface AbilityTreeAccessor {

    @Accessor(value = "selected", remap = false)
    void unobtainium$setSelected(SpecializedSkill selected);
}
