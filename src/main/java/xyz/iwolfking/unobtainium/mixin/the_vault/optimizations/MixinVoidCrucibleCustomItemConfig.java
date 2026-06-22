package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import iskallia.vault.config.crucible.VoidCrucibleCustomItemConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(value = VoidCrucibleCustomItemConfig.class, remap = false)
public class MixinVoidCrucibleCustomItemConfig {
    @Shadow
    private Map<String, VoidCrucibleCustomItemConfig.CustomItem> items;
    @Unique
    private static List<ResourceLocation> unobtainium$cachedAllItems = null;
    @Inject(method = "getAllItems", at = @At("HEAD"), cancellable = true)
    private void getCachedItems(CallbackInfoReturnable<List<ResourceLocation>> cir) {
        if (unobtainium$cachedAllItems != null) {
            cir.setReturnValue(unobtainium$cachedAllItems);
        }
    }
    @Inject(method = "readConfig", at = @At("RETURN"))
    private <T> void computeAllItems(CallbackInfoReturnable<T> cir){
        unobtainium$cachedAllItems = this.items.values().stream().map(VoidCrucibleCustomItemConfig.CustomItem::getResources).flatMap(Collection::stream).toList();
    }
}
