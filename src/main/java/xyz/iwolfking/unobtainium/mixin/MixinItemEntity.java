package xyz.iwolfking.unobtainium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class MixinItemEntity {
    @WrapOperation(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setEntityRepresentation(Lnet/minecraft/world/entity/Entity;)V"))
    private void preventEmptyStackModification(ItemStack instance, Entity pEntity, Operation<Void> original){
        if (instance == ItemStack.EMPTY) {
            return;
        }
        original.call(instance, pEntity);
    }
}
