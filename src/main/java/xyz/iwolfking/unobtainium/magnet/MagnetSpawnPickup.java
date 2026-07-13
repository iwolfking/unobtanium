package xyz.iwolfking.unobtainium.magnet;

import iskallia.vault.block.entity.DemagnetizerTileEntity;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.gear.trinket.effects.EnderAnchorTrinket;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.util.calc.MagnetRangeHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MagnetSpawnPickup {

    private static final ResourceLocation ENDERGIZED_ATTRIBUTE = new ResourceLocation("the_vault", "endergized");

    private static long cacheTick = Long.MIN_VALUE;
    private static ResourceKey<Level> cacheDim;
    private static List<Candidate> cache = List.of();

    private record Candidate(ServerPlayer player, double rangeSq) {}

    private MagnetSpawnPickup() {
    }

    @SubscribeEvent
    public static void onItemSpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }
        if (item.isRemoved() || item.getItem().isEmpty() || item.getTags().contains(MagnetItem.BLACKLIST)) {
            return;
        }
        if (tryPickup((ServerLevel) event.getWorld(), item)) {
            event.setCanceled(true); // fully consumed
        }
    }

    /**
     * emits one coalesced (possibly oversized) stack: offers the whole stack to the magnet fast-pickup in a single
     * {@code playerTouch}, then spawns whatever was not picked up, split into {@code maxStackSize} entities so no
     * over-sized item entity ever persists in the world
    */
    public static void emitCoalesced(ServerLevel level, double x, double y, double z, ItemStack stack, ServerPlayer preferred) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDefaultPickUpDelay();
        if (tryPickup(level, entity, preferred)) {
            return; // fully consumed by a magnet
        }
        spawnSplit(level, x, y, z, entity.getItem());
    }

    /** spawns {@code remaining} as one or more entities, split by {@code maxStackSize}. */
    private static void spawnSplit(ServerLevel level, double x, double y, double z, ItemStack remaining) {
        if (remaining == null || remaining.isEmpty()) {
            return;
        }
        int max = Math.max(1, remaining.getMaxStackSize());
        while (remaining.getCount() > max) {
            ItemStack piece = remaining.copy();
            piece.setCount(max);
            ItemEntity split = new ItemEntity(level, x, y, z, piece);
            split.setDefaultPickUpDelay();
            level.addFreshEntity(split);
            remaining.shrink(max);
        }
        ItemEntity last = new ItemEntity(level, x, y, z, remaining);
        last.setDefaultPickUpDelay();
        level.addFreshEntity(last);
    }

    public static boolean tryPickup(ServerLevel level, ItemEntity item) {
        return tryPickup(level, item, null);
    }

    public static boolean tryPickup(ServerLevel level, ItemEntity item, ServerPlayer preferred) {
        List<Candidate> eligible = eligible(level);
        if (eligible.isEmpty()) {
            return false;
        }

        List<ServerPlayer> candidates = null;
        for (Candidate candidate : eligible) {
            if (item.distanceToSqr(candidate.player()) > candidate.rangeSq()) {
                continue;
            }
            if (candidates == null) {
                candidates = new ArrayList<>(2);
            }
            candidates.add(candidate.player());
        }
        if (candidates == null) {
            return false;
        }
        if (candidates.size() > 1) {
            candidates.sort(Comparator.comparingDouble(item::distanceToSqr));
            if (preferred != null) {
                int idx = candidates.indexOf(preferred);
                if (idx > 0) {
                    candidates.remove(idx);
                    candidates.add(0, preferred);
                }
            }
        }

        for (ServerPlayer player : candidates) {
            if (!allowsPickup(item, player)) {
                continue;
            }
            item.setNoPickUpDelay();
            item.getTags().add(MagnetItem.PULLED);
            item.playerTouch(player);
            if (item.isRemoved() || item.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static List<Candidate> eligible(ServerLevel level) {
        long tick = level.getGameTime();
        if (tick == cacheTick && level.dimension().equals(cacheDim)) {
            return cache;
        }

        List<Candidate> result = null;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || DemagnetizerTileEntity.hasDemagnetizerAround(player)) {
                continue;
            }
            Optional<ItemStack> magnet = MagnetItem.getMagnet(player);
            if (magnet.isEmpty()) {
                continue;
            }
            ItemStack stack = magnet.get();
            if (stack.getItem() instanceof VaultGearItem gear && gear.isBroken(stack)) {
                continue;
            }
            if (!hasUsableAnchor(player) && !isEndergized(stack)) {
                continue;
            }
            float range = MagnetRangeHelper.getRange(player);
            if (result == null) {
                result = new ArrayList<>(2);
            }
            result.add(new Candidate(player, (double) range * (double) range));
        }

        cache = result == null ? List.of() : result;
        cacheTick = tick;
        cacheDim = level.dimension();
        return cache;
    }

    private static boolean hasUsableAnchor(Player player) {
        for (TrinketHelper.TrinketStack<EnderAnchorTrinket> trinket : TrinketHelper.getTrinkets(player, EnderAnchorTrinket.class)) {
            if (trinket.isUsable(player)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEndergized(ItemStack magnet) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(ENDERGIZED_ATTRIBUTE);
        if (attribute == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        VaultGearAttribute<Boolean> flag = (VaultGearAttribute<Boolean>) attribute;
        return AttributeGearData.read(magnet).get(flag, VaultGearAttributeTypeMerger.anyTrue());
    }

    private static boolean allowsPickup(ItemEntity item, Player player) {
        UUID thrower = item.getThrower();
        if (thrower != null && !thrower.equals(player.getUUID())) {
            for (Player other : player.getLevel().players()) {
                if (!other.getUUID().equals(player.getUUID())
                        && other.getBoundingBox().inflate(1.0).intersects(item.getBoundingBox())) {
                    return false;
                }
            }
        }
        return true;
    }
}
