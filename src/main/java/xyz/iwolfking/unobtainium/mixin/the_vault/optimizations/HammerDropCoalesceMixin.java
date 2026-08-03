package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.block.entity.DehammerizerTileEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.tool.IHammer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.drops.DropCoalescer;

@Mixin(ServerPlayerGameMode.class)
public abstract class HammerDropCoalesceMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void unobtainium$beginHammerAction(
        BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxBuildHeight, CallbackInfo ci) {
        if (unobtainium$hasHammer()) {
            DropCoalescer.begin(this.level, this.player);
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
    private void unobtainium$endHammerAction(
        BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxBuildHeight, CallbackInfo ci) {
        DropCoalescer.end();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void unobtainium$beginHammerTick(CallbackInfo ci) {
        if (this instanceof IHammer hammer && !hammer.getHammer().tiles.isEmpty()) {
            DropCoalescer.begin(this.level, this.player);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void unobtainium$endHammerTick(CallbackInfo ci) {
        DropCoalescer.end();
    }

    private boolean unobtainium$hasHammer() {
        ItemStack stack = this.player.getMainHandItem();
        if (stack.getItem() != ModItems.TOOL) {
            return false;
        }
        if (DehammerizerTileEntity.hasDehammerizerAround(this.player)) {
            return false;
        }
        return VaultGearData.read(stack).get(ModGearAttributes.HAMMERING, VaultGearAttributeTypeMerger.anyTrue());
    }
}
