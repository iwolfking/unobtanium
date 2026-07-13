package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import iskallia.vault.network.message.ClientboundBackpackPickupMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientboundBackpackPickupMessage.class, remap = false)
public abstract class BackpackPickupCountFixMixin {

    @Redirect(
        method = "encode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;writeItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/FriendlyByteBuf;",
            remap = true
        ),
        remap = false
    )
    private static FriendlyByteBuf unobtainium$writeBigCount(FriendlyByteBuf buffer, ItemStack stack) {
        return unobtainium$writeItemVarCount(buffer, stack);
    }

    @Redirect(
        method = "decode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;readItem()Lnet/minecraft/world/item/ItemStack;",
            remap = true
        ),
        remap = false
    )
    private static ItemStack unobtainium$readBigCount(FriendlyByteBuf buffer) {
        return unobtainium$readItemVarCount(buffer);
    }

    /** mirrors vanilla {@code writeItem} but serializes the count as a VarInt instead of a single byte. */
    @Unique
    private static FriendlyByteBuf unobtainium$writeItemVarCount(FriendlyByteBuf buffer, ItemStack stack) {
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Item item = stack.getItem();
            buffer.writeVarInt(Item.getId(item));
            buffer.writeVarInt(stack.getCount());
            CompoundTag tag = null;
            if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
                tag = stack.getTag();
            }
            buffer.writeNbt(tag);
        }
        return buffer;
    }

    /** mirrors vanilla {@code readItem} but reads the VarInt count written by {@link #unobtainium$writeItemVarCount}. */
    @Unique
    private static ItemStack unobtainium$readItemVarCount(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return ItemStack.EMPTY;
        }
        Item item = Item.byId(buffer.readVarInt());
        int count = buffer.readVarInt();
        ItemStack stack = new ItemStack(item, count);
        stack.setTag(buffer.readNbt());
        return stack;
    }
}
