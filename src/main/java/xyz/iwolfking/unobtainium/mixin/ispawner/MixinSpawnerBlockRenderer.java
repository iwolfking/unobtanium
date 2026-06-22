package xyz.iwolfking.unobtainium.mixin.ispawner;

import iskallia.ispawner.block.SurvivalSpawnerBlock;
import iskallia.ispawner.block.entity.SpawnerBlockEntity;
import iskallia.ispawner.block.render.SpawnerBlockRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ispawner")
        }
)
@Mixin(value = SpawnerBlockRenderer.class,remap = false)
public abstract class MixinSpawnerBlockRenderer<T extends SpawnerBlockEntity> implements BlockEntityRenderer<T> {

    /**
     * @author
     * @reason remove inefficient streams
     */
    @Overwrite
    private ItemStack getRenderedItem(T entity) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < entity.inventory.getContainerSize(); i++) {
            ItemStack stack = entity.inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        if (items.isEmpty()) {
            return null;
        } else {
            int i = (int)(entity.getLevel().getGameTime() / 40L % items.size());
            return items.get(i);
        }
    }

    @Inject(method = "rendersOutsideBoundingBox", at = @At("HEAD"), cancellable = true)
    private void frustumCull(SpawnerBlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir){
        if (!(blockEntity.getBlockState().getBlock() instanceof SurvivalSpawnerBlock)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 pCameraPos) {
        if (!(blockEntity.getBlockState().getBlock() instanceof SurvivalSpawnerBlock)) {
            return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(pCameraPos, 64); // default BE distance
        }
        return BlockEntityRenderer.super.shouldRender(blockEntity, pCameraPos);
    }
}
