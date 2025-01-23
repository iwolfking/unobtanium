package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.core.vault.objective.ParadoxObjective;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.entity.boss.ConjurationMagicProjectileEntity;
import iskallia.vault.entity.boss.MagicProjectileEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.mixin.AccessorChunkMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.unobtainium.mixin.the_vault.accessors.VaultChestTileEntityAccessor;

import java.util.Map;

@Mixin(value = ConjurationMagicProjectileEntity.class, remap = false)
public abstract class FixHeraldTenosPhase extends MagicProjectileEntity {

    @Shadow private Map<VaultChestType, ResourceLocation> chestLootTables;

    public FixHeraldTenosPhase(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract Block getRandomChestBlock();

    @Shadow protected abstract ResourceLocation getChestLoottable(Block chestBlock);

    /**
     * @author iwolfking
     * @reason Hacky fix for Tenos Phase chests not generating correctly initially
     */
    @Overwrite
    private void placeChest(BlockHitResult hitResult) {
        BlockPos chestPos = hitResult.getBlockPos().above();
        Block chestBlock = this.getRandomChestBlock();
        this.level.setBlockAndUpdate(chestPos, chestBlock.defaultBlockState());
        this.level.getBlockEntity(chestPos, ModBlocks.VAULT_CHEST_TILE_ENTITY).ifPresent((chest) -> {
            ((VaultChestTileEntityAccessor)chest).setVaultChest(true);
            chest.setLootTable(this.getChestLoottable(chestBlock), this.level.random.nextLong());
            chest.setChanged();
            ((ServerChunkCache) this.level.getChunkSource()).chunkMap.getPlayers(new ChunkPos(chestPos), false).forEach((player) -> {
                this.level.getChunk((new ChunkPos(chestPos)).x, (new ChunkPos(chestPos)).z, ChunkStatus.FULL, true);
                ((AccessorChunkMap) ((ServerChunkCache) this.level.getChunkSource()).chunkMap).callUpdateChunkTracking(player, new ChunkPos(chestPos), new MutableObject(), false, true);
            });
            ((ServerChunkCache) this.level.getChunkSource()).blockChanged(chestPos);
        });
    }
}
