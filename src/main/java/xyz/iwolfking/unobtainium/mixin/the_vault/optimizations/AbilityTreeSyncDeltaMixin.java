package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.Unobtanium;
import xyz.iwolfking.unobtainium.sync.AbilitySyncMessage;
import xyz.iwolfking.unobtainium.sync.ServerAbilitySync;
import xyz.iwolfking.unobtainium.sync.UnobtaniumNetwork;

@Mixin(value = AbilityTree.class, remap = false)
public class AbilityTreeSyncDeltaMixin {

    @Unique
    private final ServerAbilitySync.State unobtainium$syncState = new ServerAbilitySync.State();

    @Inject(method = "sync(Liskallia/vault/skill/base/SkillContext;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void unobtainium$deltaSync(SkillContext context, CallbackInfo ci) {
        ci.cancel(); // we fully replace the vanilla whole-tree AbilityKnownOnesMessage send

        AbilityTree self = (AbilityTree) (Object) this;
        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            try {
                byte[] payload = ServerAbilitySync.buildPayload(self, this.unobtainium$syncState, player.connection.connection);
                if (payload != null) {
                    UnobtaniumNetwork.CHANNEL.sendTo(
                        new AbilitySyncMessage(payload),
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            } catch (Exception e) {
                this.unobtainium$syncState.invalidate(); // force a full re-anchor next tick rather than desync
                Unobtanium.LOGGER.error("[unobtanium] ability-sync failed; will resend full", e);
            }
        });
    }
}
