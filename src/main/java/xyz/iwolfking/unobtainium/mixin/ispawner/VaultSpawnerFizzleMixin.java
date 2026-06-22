package xyz.iwolfking.unobtainium.mixin.ispawner;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.ispawner.block.entity.SpawnerBlockEntity;
import iskallia.ispawner.world.spawner.SpawnerExecution;
import iskallia.ispawner.world.spawner.SpawnerManager;
import iskallia.ispawner.world.spawner.SpawnerSettings;
import iskallia.vault.world.data.ServerVaults;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ispawner")
        }
)
// disables the ispawner per-category entity cap for spawners inside vault dimensions.
@Mixin(value = SpawnerManager.class, remap = false)
public class VaultSpawnerFizzleMixin {

    @Shadow
    public SpawnerSettings settings;

    @WrapMethod(method = "spawn", remap = false)
    private void unobtainium$disableCapInVaults(Level world, Random random,
                                                 SpawnerBlockEntity entity, SpawnerExecution execution,
                                                 Operation<Void> original) {
        if (ServerVaults.get(world).isEmpty()) {
            original.call(world, random, entity, execution);
            return;
        }

        Map<MobCategory, Integer> savedLimits = new HashMap<>();
        for (Map.Entry<MobCategory, SpawnerSettings.CapRestriction> e
                : settings.getCapRestrictions().entrySet()) {
            savedLimits.put(e.getKey(), e.getValue().limit);
            e.getValue().limit = 0;
        }

        try {
            original.call(world, random, entity, execution);
        } finally {
            for (Map.Entry<MobCategory, Integer> e : savedLimits.entrySet()) {
                SpawnerSettings.CapRestriction cap = settings.getCapRestrictions().get(e.getKey());
                if (cap != null) {
                    cap.limit = e.getValue();
                }
            }
        }
    }
}
