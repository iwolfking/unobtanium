package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ClientboundHunterPositionsMessage;
import iskallia.vault.skill.ability.effect.spi.HunterAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.ServerScheduler;
import iskallia.vault.world.data.ServerVaults;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.unobtainium.fixes.HunterSession;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HunterAbility.class)
public abstract class HunterRefreshMixin {

    @Shadow public abstract double getRadius(Entity attacker);

    @Shadow public abstract int getDurationTicks(LivingEntity entity);

    private static final Map<UUID, HunterSession> unobtainium$sessions = new ConcurrentHashMap<>();

    @Inject(method = "doAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void unobtainium$coalesceReveal(SkillContext context, CallbackInfoReturnable<Ability.ActionResult> cir) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player == null || !(player.getCommandSenderWorld() instanceof ServerLevel) || ServerVaults.get(player.level).isEmpty()) {
            return; // let vanilla handle the fail/miss path
        }

        long now = player.level.getGameTime();
        double radius = this.getRadius(player);
        long endTick = now + Math.max(1, this.getDurationTicks(player));

        HunterSession session = unobtainium$sessions.computeIfAbsent(player.getUUID(), uuid -> new HunterSession());
        session.radius = Math.max(session.radius, radius);
        session.endTick = Math.max(session.endTick, endTick);

        boolean activateHunter;
        synchronized (session) {
            activateHunter = !session.isRunning;
            session.isRunning = true;
        }
        if (activateHunter) {
            unobtanium$recurseHunter(player);
        }

        cir.setReturnValue(Ability.ActionResult.successCooldownImmediate());
    }

    private static void unobtanium$recurseHunter(ServerPlayer player) {
        UUID uuid = player.getUUID();
        HunterSession session = unobtainium$sessions.get(uuid);
        if (session == null) {
            return;
        }

        long now = player.level.getGameTime();
        if (player.isRemoved() || !(player.level instanceof ServerLevel world) || ServerVaults.get(player.level).isEmpty() || now >= session.endTick) {
            unobtainium$sessions.remove(uuid);
            return;
        }

        List<HunterAbility.HighlightPosition> positions = HunterAbility.selectPositions(world, player, session.radius);
        ModNetwork.CHANNEL.sendTo(
            new ClientboundHunterPositionsMessage(positions), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT
        );

        ServerScheduler.INSTANCE.schedule(5, () -> unobtanium$recurseHunter(player));
    }
}
