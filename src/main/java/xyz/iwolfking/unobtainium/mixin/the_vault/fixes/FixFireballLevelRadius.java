package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.skill.ability.effect.spi.AbstractFireballAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

/*
The original Fireball code always passes 3.0F in instead of the right radius for your level of Fireball, this passes the correct radius in based on your level instead.
*/
@Mixin(value = VaultFireball.class, remap = false)
public abstract class FixFireballLevelRadius extends AbstractArrow{

    protected FixFireballLevelRadius(EntityType<? extends AbstractArrow> p_36721_, Level p_36722_) {
        super(p_36721_, p_36722_);
    }

    @Shadow
    public abstract VaultFireball.FireballType getFireballType();


    @Redirect(method = "explode", at = @At(value = "INVOKE",target = "Liskallia/vault/util/calc/AreaOfEffectHelper;adjustAreaOfEffectKey(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;F)F"))
    public float explode(LivingEntity entity, String abilityKey, float range) {
        if(entity instanceof Player player) {
            return AreaOfEffectHelper.adjustAreaOfEffectKey(player, this.getFireballType().getAbilityName(), woldsVaults_Dev$getRadius());
        }

        return AreaOfEffectHelper.adjustAreaOfEffectKey(entity, this.getFireballType().getAbilityName(), 3.0F);
    }


    @Unique
    public float woldsVaults_Dev$getRadius() {
        Entity var2 = this.getOwner();
        if (var2 instanceof ServerPlayer serverPlayer) {
            AbilityTree abilities = PlayerAbilitiesData.get(serverPlayer.getLevel()).getAbilities(serverPlayer);
            Iterator var3 = abilities.getAll(AbstractFireballAbility.class, Skill::isUnlocked).iterator();
            if (var3.hasNext()) {
                AbstractFireballAbility ability = (AbstractFireballAbility)var3.next();
                return ability.getRadius();
            }
        }

        return 3.0F;
    }
}
