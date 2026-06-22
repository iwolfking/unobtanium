package xyz.iwolfking.unobtainium.mixin.ispawner;

import iskallia.ispawner.init.ModConfigs;
import iskallia.ispawner.item.GenericSpawnEggItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ispawner")
        }
)
@Mixin(value = GenericSpawnEggItem.class, remap = false)
public class MixinGenericSpawnEggItem {
    /**
     * @author
     * @reason improve performance by removing useless color calculations
     */
    @Overwrite
    public static int getColor(ItemStack stack, int tintIndex) {
        return tintIndex == 0 ? 16777215 : 0;
    }
}
