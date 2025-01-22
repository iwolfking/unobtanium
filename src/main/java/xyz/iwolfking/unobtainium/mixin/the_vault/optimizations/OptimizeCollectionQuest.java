package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.quest.type.CollectionQuest;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.unobtainium.optimizations.UnsafeInventoryUtil;

import java.util.List;
import java.util.Map;

@Mixin(value = CollectionQuest.class, remap = false)
public class OptimizeCollectionQuest {
    @Redirect(method = "checkCollections", at = @At(value = "INVOKE", target = "Liskallia/vault/util/InventoryUtil;findAllItems(Lnet/minecraft/world/entity/player/Player;)Ljava/util/List;"))
    private static List<InventoryUtil.ItemAccess> checkCollections(Player inventoryFn) {
        return List.of();
    }

    @Inject(method = "checkCollections", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static void checkCollections(ServerPlayer sPlayer, CallbackInfo ci,
                                         @Local(ordinal = 0) Map<Item, List<CollectionQuest>> itemQuestMap,
                                         @Local(ordinal = 1) Map<CollectionQuest, Integer> countMap) {
        UnsafeInventoryUtil.findAllItems(sPlayer)
            .stream()
            .filter(stack -> itemQuestMap.containsKey(stack.getItem()))
            .forEach(stack -> {
                List<CollectionQuest> quests = itemQuestMap.get(stack.getItem());
                if (quests != null) {
                    quests.forEach(quest -> {
                        int existing = countMap.getOrDefault(quest, 0);
                        countMap.put(quest, existing + stack.getCount());
                    });
                }
            });
    }

    // we can run it on every tick when it's not slow anymore
    @ModifyConstant(method = "onTick", constant = @Constant(intValue = 20))
    private static int onTick(int original) {
        return 1;
    }

}
