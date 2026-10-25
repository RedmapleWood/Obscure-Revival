package redmaple.obscurerevival.recipe.input;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record FletchingRecipeInput(ItemStack blueprint, ItemStack base, ItemStack addition) implements RecipeInput {

    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> this.blueprint;
            case 1 -> this.base;
            case 2 -> this.addition;
            default -> throw new IllegalArgumentException("Recipe does not contain slot " + slot);
        };
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return this.blueprint.isEmpty() && this.base.isEmpty() && this.addition.isEmpty();
    }
}