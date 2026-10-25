package redmaple.obscurerevival.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import redmaple.obscurerevival.recipe.input.FletchingRecipeInput;
import redmaple.obscurerevival.registry.ORRecipeTypes;

public interface FletchingRecipe extends Recipe<FletchingRecipeInput> {

    @Override
    default RecipeType<FletchingRecipe> getType() { return ORRecipeTypes.FLETCHING; }

    @Override
    default RecipeBookCategory getRecipeBookCategory() { return ORRecipeTypes.FLETCHING_RECIPE_BOOK_CATEGORY; }

    @Override
    boolean matches(FletchingRecipeInput input, World world);

    @Override
    ItemStack craft(FletchingRecipeInput input, RegistryWrapper.WrapperLookup registries);
}