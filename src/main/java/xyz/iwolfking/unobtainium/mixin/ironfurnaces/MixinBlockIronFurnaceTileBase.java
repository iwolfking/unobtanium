package xyz.iwolfking.unobtainium.mixin.ironfurnaces;

import com.llamalad7.mixinextras.sugar.Local;
import ironfurnaces.tileentity.TileEntityInventory;
import ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static xyz.iwolfking.unobtainium.fixes.FixIronFurnacesLag.HAS_RECIPE;
import static xyz.iwolfking.unobtainium.fixes.FixIronFurnacesLag.HAS_RECIPE_BLASTING;
import static xyz.iwolfking.unobtainium.fixes.FixIronFurnacesLag.HAS_RECIPE_SMOKING;
import static xyz.iwolfking.unobtainium.fixes.FixIronFurnacesLag.SMOKING_BURNS;

// backported caches from iron furnaces 4.1.6
@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ironfurnaces")
    }
)
@Mixin(value = BlockIronFurnaceTileBase.class, remap = false)
public abstract class MixinBlockIronFurnaceTileBase extends TileEntityInventory {
    @Shadow public RecipeType<? extends AbstractCookingRecipe> recipeType;

    public MixinBlockIronFurnaceTileBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos,
                                         BlockState state, int sizeInventory) {
        super(tileEntityTypeIn, pos, state, sizeInventory);
    }

    /**
     * @author radimous
     * @reason improve perf
     */
    @Overwrite
    public boolean hasRecipe(ItemStack stack) {

        Item item = stack.getItem();
        if (recipeType == RecipeType.SMOKING) {
            return HAS_RECIPE_SMOKING.computeIfAbsent(item, (value) -> this.level.getRecipeManager()
                .getRecipeFor(recipeType, new SimpleContainer(stack), this.level).isPresent());
        } else if (recipeType == RecipeType.BLASTING) {
            return HAS_RECIPE_BLASTING.computeIfAbsent(item, (value) -> this.level.getRecipeManager()
                .getRecipeFor(recipeType, new SimpleContainer(stack), this.level).isPresent());
        }
        return HAS_RECIPE.computeIfAbsent(item, (value) -> this.level.getRecipeManager()
            .getRecipeFor(recipeType, new SimpleContainer(stack), this.level).isPresent());

    }

    /**
     * @author radimous
     * @reason improve perf
     */
    @Overwrite
    public static int getSmokingBurn(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        return SMOKING_BURNS.getOrDefault(item, unobtainium$addSmokingBurn(stack));
    }

    @Unique private static int unobtainium$addSmokingBurn(ItemStack stack) {
        int burnTime = unobtainium$getSmokingBurnTime(stack);
        Item item = stack.getItem();
        SMOKING_BURNS.put(item, burnTime);
        return 0;
    }

    @Unique private static int unobtainium$getSmokingBurnTime(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem().getFoodProperties() != null) {
                if (stack.getItem().getFoodProperties().getNutrition() > 0) {
                    return stack.getItem().getFoodProperties().getNutrition() * 800;
                }
            }
        }
        return 0;
    }

    @Redirect(method = {"getGeneratorBurn", "load", "IisItemValidForSlot"}, at = @At(value = "INVOKE", target = "Lironfurnaces/tileentity/furnaces/BlockIronFurnaceTileBase;getBurnTime(Lnet/minecraft/world/item/ItemStack;)I"))
    private int unobtainium$getBurnTime(ItemStack stack) {
       return ForgeHooks.getBurnTime(stack, this.recipeType);
    }

    @Redirect(method = {"tick"}, at = @At(value = "INVOKE", target = "Lironfurnaces/tileentity/furnaces/BlockIronFurnaceTileBase;getBurnTime(Lnet/minecraft/world/item/ItemStack;)I"))
    private static int unobtainium$getBurnTimeTick(ItemStack stack,  @Local(argsOnly = true) BlockIronFurnaceTileBase e) {
        return ForgeHooks.getBurnTime(stack, e.recipeType);
    }
}