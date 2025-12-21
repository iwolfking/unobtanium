package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.util.NetcodeUtils;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class FixLevelCap {
    @Shadow
    private int vaultLevel;

    @Shadow
    private int exp;
    @Shadow
    @Final
    private UUID uuid;

    @Shadow
    public abstract void sync(MinecraftServer server);

    @Inject(method = "addVaultExp", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/PlayerVaultStats;getExpNeededToNextLevel()I", shift = At.Shift.BEFORE), cancellable = true)
    private void fixLevelCapIssue(MinecraftServer server, int exp, CallbackInfo ci, @Local(ordinal = 1) int maxLevel, @Local(ordinal = 2) int initialLevel) {
        if(this.vaultLevel >= maxLevel && !(this.vaultLevel >= 100)) {
            this.exp = 0;
            this.vaultLevel = maxLevel;
            if (this.vaultLevel > initialLevel) {
                NetcodeUtils.runIfPresent(server, this.uuid, this::fancyLevelUpEffects);
                ServerPlayer player = server.getPlayerList().getPlayer(this.uuid);
                if (player != null) {
                    player.refreshTabListName();
                }

                CommonEvents.VAULT_LEVEL_UP.invoke(player, exp, initialLevel, this.vaultLevel);
            }
            this.sync(server);
            ci.cancel();
        }
    }

    @Inject(method = "addVaultExp", at = @At("HEAD"), cancellable = true)
    private void preventXpOverflow(MinecraftServer server, int exp, CallbackInfo ci) {
        int maxLevel = ModConfigs.LEVELS_META.getMaxLevel();
        if(this.vaultLevel >= maxLevel && !(this.vaultLevel >= 100)) {
            this.exp = 0;
            this.vaultLevel = maxLevel;
            this.sync(server);
            ci.cancel();
        }
    }

    @Shadow
    protected abstract void fancyLevelUpEffects(ServerPlayer player);
}
