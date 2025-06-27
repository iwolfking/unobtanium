package xyz.iwolfking.unobtainium.mixin.ironfurnaces;

import ironfurnaces.jei.IronFurnacesJEIPlugin;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// backported caches from iron furnaces 4.1.
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ironfurnaces")
    }
)
@Mixin(value = IronFurnacesJEIPlugin.class, remap = false)
public class MixinIronFurnacesJEIPlugin {
    @Redirect(method = "registerRecipes", at = @At(value = "INVOKE", target = "Lironfurnaces/tileentity/furnaces/BlockIronFurnaceTileBase;getBurnTime(Lnet/minecraft/world/item/ItemStack;)I"))
    private int redirectGetBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }
}
