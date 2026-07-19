package xyz.iwolfking.unobtainium.drops;

import iskallia.vault.init.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.iwolfking.unobtainium.magnet.MagnetSpawnPickup;

//collects the side effects of a multi-block break burst so they emit once at the end instead of once per block
public final class DropCoalescer {

    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();

    private DropCoalescer() {
    }

    public static void foldOversized(List<ItemStack> out, Map<Item, List<ItemStack>> index, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!stack.isStackable()) {
            out.add(stack.copy());
            return;
        }
        List<ItemStack> bucket = index.computeIfAbsent(stack.getItem(), k -> new ArrayList<>(1));
        for (ItemStack existing : bucket) {
            if (ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                existing.setCount(existing.getCount() + stack.getCount());
                return;
            }
        }
        ItemStack copy = stack.copy();
        out.add(copy);
        bucket.add(copy);
    }

    private static final class Session {
        final ServerLevel level;
        int depth;
        List<ItemStack> buffer;
        BlockPos anchor;
        int experience;
        int copiousProcs;
        ServerPlayer breaker;
        Map<Item, List<ItemStack>> stackIndex;

        Session(ServerLevel level) {
            this.level = level;
        }
    }

    public static boolean isActive(Level level) {
        Session session = SESSION.get();
        return session != null && session.level == level;
    }

    public static void begin(Level level) {
        begin(level, null);
    }

    public static void begin(Level level, Player breaker) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Session session = SESSION.get();
        if (session == null) {
            session = new Session(serverLevel);
            SESSION.set(session);
        }
        if (session.breaker == null && breaker instanceof ServerPlayer serverPlayer) {
            session.breaker = serverPlayer;
        }
        session.depth++;
    }

    public static void end() {
        Session session = SESSION.get();
        if (session == null) {
            return;
        }
        if (--session.depth > 0) {
            return;
        }

        SESSION.remove();
        flush(session);
    }

    public static boolean capture(Level level, BlockPos pos, ItemStack stack) {
        if (!isActive(level) || stack == null || stack.isEmpty()) {
            return false;
        }
        Session session = SESSION.get();
        if (session.buffer == null) {
            session.buffer = new ArrayList<>();
            session.stackIndex = new HashMap<>();
            session.anchor = pos.immutable();
        }
        foldOversized(session.buffer, session.stackIndex, stack);
        return true;
    }

    public static boolean addExperience(Level level, BlockPos pos, int amount) {
        Session session = SESSION.get();
        if (session == null || session.level != level || amount <= 0) {
            return false;
        }
        if (session.anchor == null) {
            session.anchor = pos.immutable();
        }
        session.experience += amount;
        return true;
    }

    public static void addCopiousProcs(Level level, int procs) {
        Session session = SESSION.get();
        if (session == null || session.level != level || procs <= 0) {
            return;
        }
        session.copiousProcs += procs;
    }

    private static void flush(Session session) {
        if (session.anchor == null) {
            return; // nothing was captured this burst (no drops, no xp) — nothing to place
        }
        double x = session.anchor.getX() + 0.5;
        double y = session.anchor.getY() + 0.5;
        double z = session.anchor.getZ() + 0.5;

        if (session.buffer != null) {
            for (ItemStack stack : session.buffer) {
                MagnetSpawnPickup.emitCoalesced(session.level, x, y, z, stack, session.breaker);
            }
        }

        if (session.experience > 0) {
            session.level.addFreshEntity(new ExperienceOrb(session.level, x, y, z, session.experience));
        }

        if (session.copiousProcs > 0) {
            float volume = (float) Math.min(1.0, 0.1 * (1.0 + Math.log(session.copiousProcs)));
            session.level.playSound(null, session.anchor, ModSounds.VAULT_CHEST_OMEGA_OPEN, SoundSource.BLOCKS, volume, 0.85F);
        }
    }
}
