package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.core.Version;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.storage.VirtualWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.sync.ServerFieldSync;

@Mixin(value = Vault.class, remap = false)
public class VaultBatchSyncMixin {

    @Unique
    private final ServerFieldSync.State unobtainium$syncState = new ServerFieldSync.State();

    @Inject(method = "tickServer(Liskallia/vault/core/world/storage/VirtualWorld;)V", at = @At("TAIL"), remap = false)
    private void unobtainium$snapshotSync(VirtualWorld world, CallbackInfo ci) {
        Vault self = (Vault) (Object) this;
        if (!self.has(Vault.LISTENERS) || !self.has(Vault.VERSION)) {
            return;
        }

        Version version = self.get(Vault.VERSION);
        Listeners listeners = self.get(Vault.LISTENERS);

        List<ServerPlayer> eligible = new ArrayList<>();
        for (Listener listener : listeners.getAll()) {
            Optional<ServerPlayer> maybePlayer = listener.getPlayer();
            if (maybePlayer.isEmpty()) {
                continue;
            }
            ServerPlayer player = maybePlayer.get();
            if (player.isDeadOrDying()) {
                continue; // mirror vanilla guard on the now-cancelled Listener.tickServer() sync msg
            }
            eligible.add(player);
        }

        ServerFieldSync.sync(self, version, eligible, this.unobtainium$syncState);
    }
}
