package redmaple.obscurerevival.data.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.recipe.FletchingTransformRecipe;

public class FletchingTransformRecipeJsonBuilder {
    private final Ingredient blueprint;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    public FletchingTransformRecipeJsonBuilder(Ingredient blueprint, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        this.category = category;
        this.blueprint = blueprint;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static FletchingTransformRecipeJsonBuilder create(Ingredient blueprint, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
        return new FletchingTransformRecipeJsonBuilder(blueprint, base, addition, category, result);
    }

    public FletchingTransformRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public void offerTo(RecipeExporter exporter, String recipeId) {
        this.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(recipeId)));
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        this.validate(recipeKey);

        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .criteriaMerger(CriterionMerger.OR);
        this.criteria.forEach(builder::criterion);

        FletchingTransformRecipe fletchingTransformRecipe = new FletchingTransformRecipe(
                Optional.of(this.blueprint),
                this.base,
                Optional.of(this.addition),
                new TransmuteRecipeResult(this.result)
        );

        exporter.accept(recipeKey, fletchingTransformRecipe, builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
        }
    }
}