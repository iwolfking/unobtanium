package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.nbt.VListNBT;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.unobtainium.fixes.DirtyData;

import java.util.UUID;

@Mixin(value = VaultPartyData.class, remap = false)
public abstract class FixPartySaving extends SavedData {
    @Inject(method = "createParty", at = @At(value = "INVOKE", target = "Liskallia/vault/nbt/VListNBT;add(Ljava/lang/Object;)Z"))
    private void createSetDirty(UUID playerId, CallbackInfoReturnable<Boolean> cir){
        this.setDirty();
    }

    @Inject(method = "disbandParty", at = @At(value = "INVOKE", target = "Liskallia/vault/nbt/VListNBT;remove(Ljava/lang/Object;)Z"))
    private void disbandSetDirty(UUID playerId, CallbackInfoReturnable<Boolean> cir){
        this.setDirty();
    }

    @Inject(method = "lambda$onServerTick$2", at = @At(value = "TAIL"))
    private static void modifySetDirty(MinecraftServer serverInstance, VaultPartyData.Party party, CallbackInfo ci){
        VaultPartyData vaultPartyData = VaultPartyData.get(serverInstance);
        if (party instanceof DirtyData dirtyData && dirtyData.unobtainium$isDirty()) {
            dirtyData.unobtainium$setDirty(false);
            vaultPartyData.setDirty();
        }
    }

    @Mixin(value = VaultPartyData.Party.class, remap = false)
    private static class MixinInnerParty implements DirtyData {
        @Inject(method = "addMember", at = @At("TAIL"))
        private void addSetDirty(UUID member, CallbackInfoReturnable<Boolean> cir){
            unobtainium$setDirty();
        }
        @Inject(method = "invite", at = @At(value = "INVOKE", target = "Liskallia/vault/nbt/VListNBT;add(Ljava/lang/Object;)Z"))
        private void inviteSetDirty(UUID member, CallbackInfoReturnable<Boolean> cir){
            unobtainium$setDirty();
        }
        @Inject(method = "remove", at = @At(value = "TAIL"))
        private void removeSetDirty(UUID member, CallbackInfoReturnable<Boolean> cir, @Local(name = "removed") boolean removed){
            if (removed)
                unobtainium$setDirty();
        }
        @Inject(method = "confirmInvite", at = @At(value = "INVOKE", target = "Liskallia/vault/nbt/VListNBT;add(Ljava/lang/Object;)Z"))
        private void confirmInviteSetDirty(UUID member, CallbackInfoReturnable<Boolean> cir){
            unobtainium$setDirty();
        }

        @WrapOperation(method = "removeInvite", at = @At(value = "INVOKE", target = "Liskallia/vault/nbt/VListNBT;remove(Ljava/lang/Object;)Z"))
        private boolean removeInviteSetDirty(VListNBT instance, Object o, Operation<Boolean> original){
            var removed = original.call(instance, o);
            if (removed) {
                unobtainium$setDirty();
            }
            return removed;
        }

        // bool for tracking
        @Unique
        private boolean unobtainium$dirty = false;
        @Override
        public boolean unobtainium$isDirty() {
            return unobtainium$dirty;
        }

        @Override
        public void unobtainium$setDirty() {
            unobtainium$setDirty(true);
        }

        @Override
        public void unobtainium$setDirty(boolean dirty) {
            this.unobtainium$dirty = dirty;
        }

    }
}
