package redmaple.obscurerevival.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import redmaple.obscurerevival.recipe.display.FletchingRecipeDisplay;
import redmaple.obscurerevival.recipe.input.FletchingRecipeInput;
import redmaple.obscurerevival.registry.ORItems;
import redmaple.obscurerevival.registry.ORRecipeTypes;
import redmaple.obscurerevival.screen.FletchingScreenHandler;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FletchingTransformRecipe implements FletchingRecipe {

    final Optional<Ingredient> blueprint;
    final Ingredient base;
    final Optional<Ingredient> addition;
    final TransmuteRecipeResult result;
    private @Nullable IngredientPlacement ingredientPlacement;

    public FletchingTransformRecipe(
            Optional<Ingredient> blueprint,
            Ingredient base,
            Optional<Ingredient> addition,
            TransmuteRecipeResult result
    ) {
        this.blueprint = blueprint;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(FletchingRecipeInput input, World world) {
        if (!Ingredient.matches(this.blueprint(), input.blueprint())) {
            return false;
        }
        if (!this.base().test(input.base())) {
            return false;
        }
        if (!Ingredient.matches(this.addition(), input.addition())) {
            return false;
        }

        if (this.result.itemEntry().value() == ORItems.REPEATING_CROSSBOW) {
            return !FletchingScreenHandler.hasMultishotEnchantment(input.base());
        }

        return true;
    }

    @Override
    public ItemStack craft(FletchingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack output = this.result.apply(input.base());

        if (output.isOf(ORItems.REPEATING_CROSSBOW)) {
            stripMultishotEnchantment(output);
        }

        return output;
    }

    private static void stripMultishotEnchantment(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
            builder.remove(entry -> entry.matchesKey(Enchantments.MULTISHOT));
            stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        }
    }

    public Optional<Ingredient> blueprint() {
        return this.blueprint;
    }

    public Ingredient base() {
        return this.base;
    }

    public Optional<Ingredient> addition() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<FletchingTransformRecipe> getSerializer() {
        return ORRecipeTypes.FLETCHING_TRANSFORM_SERIALIZER;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forMultipleSlots(
                    List.of(this.blueprint(), Optional.of(this.base()), this.addition())
            );
        }
        return this.ingredientPlacement;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new FletchingRecipeDisplay(
                Ingredient.toDisplay(this.blueprint()),
                this.base().toDisplay(),
                Ingredient.toDisplay(this.addition()),
                this.result.createSlotDisplay(),
                new SlotDisplay.ItemSlotDisplay(Items.FLETCHING_TABLE)
        ));
    }

    public static class Serializer implements RecipeSerializer<FletchingTransformRecipe> {

        private static final MapCodec<FletchingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC.optionalFieldOf("blueprint").forGetter(FletchingTransformRecipe::blueprint),
                        Ingredient.CODEC.fieldOf("base").forGetter(FletchingTransformRecipe::base),
                        Ingredient.CODEC.optionalFieldOf("addition").forGetter(FletchingTransformRecipe::addition),
                        TransmuteRecipeResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                ).apply(instance, FletchingTransformRecipe::new)
        );

        public static final PacketCodec<RegistryByteBuf, FletchingTransformRecipe> PACKET_CODEC = PacketCodec.tuple(
                Ingredient.OPTIONAL_PACKET_CODEC, FletchingTransformRecipe::blueprint,
                Ingredient.PACKET_CODEC, FletchingTransformRecipe::base,
                Ingredient.OPTIONAL_PACKET_CODEC, FletchingTransformRecipe::addition,
                TransmuteRecipeResult.PACKET_CODEC, recipe -> recipe.result,
                FletchingTransformRecipe::new
        );

        @Override
        public MapCodec<FletchingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, FletchingTransformRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}