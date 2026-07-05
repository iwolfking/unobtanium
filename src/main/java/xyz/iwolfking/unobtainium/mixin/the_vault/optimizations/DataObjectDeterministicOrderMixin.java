package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;

import java.util.HashSet;
import java.util.LinkedHashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * redirect the HashSet to a LinkedHashSet, to preserve insertion order
 * so that the the fields are serialized deterministically, to make the snapshot/delta work.
 * affects every IDataObject.write/writeDiff but is purely an ordering change. _should_ be safe
 */
@Mixin(targets = "iskallia.vault.core.data.IDataObject$ValueMap", remap = false)
public class DataObjectDeterministicOrderMixin {

    @Redirect(method = "entrySetTyped", at = @At(value = "NEW", target = "java/util/HashSet"), remap = false)
    private HashSet<?> unobtainium$deterministicEntryOrder() {
        return new LinkedHashSet<>();
    }
}
