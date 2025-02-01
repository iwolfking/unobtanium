package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.quest.QuestConfig;
import iskallia.vault.quest.type.CollectionQuest;
import iskallia.vault.world.data.QuestStatesData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CollectionQuest.class, remap = false)
public class FixSkyQuests {

    // Before it always returned the regular QuestConfig, even if player was in skyvaults
    // Now it returns the correct config
    @Redirect(method = "checkCollections", at = @At(value = "FIELD", target = "Liskallia/vault/init/ModConfigs;QUESTS:Liskallia/vault/config/quest/QuestConfig;"))
    private static QuestConfig checkCollections(@Local(argsOnly = true) ServerPlayer sPlayer) {
        return QuestStatesData.get().getState(sPlayer).getConfig(sPlayer.getLevel());
    }
}
