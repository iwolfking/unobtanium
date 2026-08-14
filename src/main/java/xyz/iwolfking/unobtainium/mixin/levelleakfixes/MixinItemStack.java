package xyz.iwolfking.unobtainium.mixin.levelleakfixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Shadow
    @Nullable
    private CompoundTag tag;

    @Inject(method = "setEntityRepresentation", at = @At("HEAD"))
    private void checkEmptyEntity(Entity pEntity, CallbackInfo ci){
        if (pEntity != null && (Object)this == ItemStack.EMPTY) {
            throw new IllegalStateException("Someone tried to poison ItemStack.EMPTY with this non empty entity representation: " + pEntity);
        }
    }

    @Inject(method = "setTag", at = @At("HEAD"))
    private void checkEmptyTag(CompoundTag p_41752_, CallbackInfo ci){
        if ((Object)this == ItemStack.EMPTY && p_41752_ != null) {
            throw new IllegalStateException("Someone tried to poison ItemStack.EMPTY with this non empty tag: " + p_41752_);

        }
    }
}
