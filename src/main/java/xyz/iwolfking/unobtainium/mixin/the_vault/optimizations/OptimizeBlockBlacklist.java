package xyz.iwolfking.unobtainium.mixin.the_vault.optimizations;


import iskallia.vault.util.GlobUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//Credits to rizek/rad_ju/rad for this! Originally from "VH Globber"
/*
Fixes a lot of lag involving hammers by caching the regex used for checking the vault's block blacklist config.
*/
@Mixin(value = GlobUtils.class, remap = false, priority = 1500)
public class OptimizeBlockBlacklist {
    @Unique
    private static Map<String, Pattern> unobtainium$glob2pattern = new HashMap<>();

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void cachedMatches(String glob, String text, CallbackInfoReturnable<Boolean> cir){
        Pattern pattern = unobtainium$glob2pattern.get(glob);
        if (pattern != null) {
            cir.setReturnValue(pattern.matcher(text).matches());
        }
    }

    @Inject(method = "matches", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void cachePattern(String glob, String text, CallbackInfoReturnable<Boolean> cir, Pattern pattern){
        unobtainium$glob2pattern.put(glob, pattern);
    }
}
