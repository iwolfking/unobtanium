package xyz.iwolfking.unobtainium.mixin.the_vault.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.common.entity.familiar.FamiliarEntity;
import iskallia.vault.config.EntityGroupsConfig;
import iskallia.vault.core.world.data.entity.PartialEntity;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
    The Entity Groups config does not take kindly to some of Ars Nouveau's entities.
    This patches the Entity Groups config when the mod is present, fixing the issue for you and allowing Ars to work correctly.
*/
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ars_nouveau")
        }
)
@Mixin(value = EntityGroupsConfig.class, remap = false)
public class MixinEntityGroupsConfig {
    @Inject(method = "isInGroup(Lnet/minecraft/resources/ResourceLocation;Liskallia/vault/core/world/data/entity/PartialEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void dontHandleArsEntities(ResourceLocation groupId, PartialEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity.getId().getPath().equals("ars_nouveau")) {
            cir.setReturnValue(false);
            return;
        }
    }

    @Inject(method = "isInGroup(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void dontHandleArsEntities(ResourceLocation groupId, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof FamiliarEntity) {
            cir.setReturnValue(false);
            return;
        }
    }
}
