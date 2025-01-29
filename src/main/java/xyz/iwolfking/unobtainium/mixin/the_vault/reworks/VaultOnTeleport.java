package xyz.iwolfking.unobtainium.mixin.the_vault.reworks;


import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ClassicListenersLogic.class)
public abstract class VaultOnTeleport {
    @Shadow public abstract String getVaultObjective(String key);

    @Inject(method = "onTeleport",
            at = @At("TAIL"),
            remap = false)
    private void modifyOnTeleport(VirtualWorld world, Vault vault, ServerPlayer player, CallbackInfo ci) {
        String objective = getVaultObjective(((Objectives) vault.get(Vault.OBJECTIVES)).get(Objectives.KEY));
        ResourceLocation theme = ((WorldManager) vault.get(Vault.WORLD)).get(WorldManager.THEME);
        Optional<ThemeKey> themeKey = Optional.ofNullable((ThemeKey)VaultRegistry.THEME.getKey(theme));

        MutableComponent title = new TextComponent(objective)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(14536734)));

        MutableComponent subtitle = new TextComponent(themeKey.map(ThemeKey::getName).orElse("Unknown"))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(themeKey.map(ThemeKey::getColor).orElse(16777215))));

        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }
}