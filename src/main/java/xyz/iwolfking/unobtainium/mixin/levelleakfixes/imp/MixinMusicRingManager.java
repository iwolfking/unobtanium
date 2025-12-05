package xyz.iwolfking.unobtainium.mixin.levelleakfixes.imp;

import com.llamalad7.mixinextras.sugar.Local;
import dev.felnull.imp.server.music.ringer.MusicRing;
import dev.felnull.imp.server.music.ringer.MusicRingManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(value = MusicRingManager.class, remap = false)
public class MixinMusicRingManager {
    @Mutable @Shadow @Final private Map<ServerLevel, MusicRing> MUSIC_RINGS;
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void removeAlloc(CallbackInfo ci){
        MUSIC_RINGS = new WeakHashMap<>();
    }
}
