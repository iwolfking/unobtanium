package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class FixLevelCap {
    @Shadow
    private int vaultLevel;

    @Shadow
    private int exp;

    @Shadow
    public abstract void sync(MinecraftServer server);

    @Inject(method = "addVaultExp", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/PlayerVaultStats;getExpNeededToNextLevel()I", shift = At.Shift.BEFORE), cancellable = true)
    private void fixLevelCapIssue(MinecraftServer server, int exp, CallbackInfo ci, @Local(ordinal = 1) int maxLevel) {
        if(this.vaultLevel >= maxLevel && !(this.vaultLevel >= 100)) {
            this.exp = 0;
            this.vaultLevel = maxLevel;
            this.sync(server);
            ci.cancel();
        }
    }
}
