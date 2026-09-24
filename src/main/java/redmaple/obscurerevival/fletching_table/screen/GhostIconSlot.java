package redmaple.obscurerevival.fletching_table.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * 空槏位时按条件显示提示图标的槏位。
 * showCondition 返回 true 才显示图标；用 BooleanSupplier 而不是写死逻辑，
 * 是为了让"箭矢槏一直显示"和"材料槏要等箭矢放入才显示"共用同一个类。
 */
public class GhostIconSlot extends Slot {
    private final Identifier backgroundSprite;
    private final BooleanSupplier showCondition;
    private final Predicate<ItemStack> acceptPredicate;

    public GhostIconSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSprite, BooleanSupplier showCondition, Predicate<ItemStack> acceptPredicate) {
        super(inventory, index, x, y);
        this.backgroundSprite = backgroundSprite;
        this.showCondition = showCondition;
        this.acceptPredicate = acceptPredicate;
    }

    @Override
    public @Nullable Identifier getBackgroundSprite() { return this.showCondition.getAsBoolean() ? this.backgroundSprite : null; }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.acceptPredicate.test(stack);
    }
}