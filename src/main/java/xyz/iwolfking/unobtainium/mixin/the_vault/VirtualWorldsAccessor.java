package xyz.iwolfking.unobtainium.mixin.the_vault;

import iskallia.vault.core.world.threading.ThreadPool;
import iskallia.vault.world.data.VirtualWorlds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VirtualWorlds.class)
public interface VirtualWorldsAccessor {
    @Accessor
    static ThreadPool getCONCURRENT_POOL() {throw new UnsupportedOperationException();}
    @Accessor
    static void setCONCURRENT_POOL(ThreadPool threadPool) {throw new UnsupportedOperationException();}
}
