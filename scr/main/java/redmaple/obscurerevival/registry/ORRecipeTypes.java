package redmaple.obscurerevival.registry;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.ObscureRevival;
import redmaple.obscurerevival.recipe.FletchingRecipe;
import redmaple.obscurerevival.recipe.FletchingTransformRecipe;
import redmaple.obscurerevival.recipe.display.FletchingRecipeDisplay;

public final class ORRecipeTypes {

    public static final RecipeBookCategory FLETCHING_RECIPE_BOOK_CATEGORY =
            Registry.register(Registries.RECIPE_BOOK_CATEGORY, Identifier.of(ObscureRevival.MOD_ID, "fletching"), new RecipeBookCategory());

    public static final RecipeType<FletchingRecipe> FLETCHING =
            Registry.register(Registries.RECIPE_TYPE, Identifier.of(ObscureRevival.MOD_ID, "fletching"), new RecipeType<>() {
                @Override
                public String toString() {
                    return ObscureRevival.MOD_ID + ":fletching";
                }
            });

    public static final RecipeSerializer<FletchingTransformRecipe> FLETCHING_TRANSFORM_SERIALIZER =
            Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(ObscureRevival.MOD_ID, "fletching_transform"), new FletchingTransformRecipe.Serializer());

    public static void register() {
        // 内联注册 Display Serializer，消除 IDE "未使用字段" 警告
        Registry.register(
                Registries.RECIPE_DISPLAY,
                Identifier.of(ObscureRevival.MOD_ID, "fletching_transform"),
                FletchingRecipeDisplay.SERIALIZER
        );

        ObscureRevival.LOGGER.info("\"Recipe Types, Categories & Serializers\" registered successfully!");
    }

    private ORRecipeTypes() {}
}