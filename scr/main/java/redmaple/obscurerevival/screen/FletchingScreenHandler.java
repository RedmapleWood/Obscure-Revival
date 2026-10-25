package redmaple.obscurerevival.screen;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import redmaple.obscurerevival.recipe.FletchingRecipe;
import redmaple.obscurerevival.registry.ORRecipeTypes;
import redmaple.obscurerevival.recipe.input.FletchingRecipeInput;
import redmaple.obscurerevival.registry.ORItems;
import redmaple.obscurerevival.registry.ORScreenHandlerTypes;
import redmaple.obscurerevival.registry.ORSounds;

public class FletchingScreenHandler extends ForgingScreenHandler {

    public static final Text TITLE = Text.translatable("container.obscurerevival.modify");

    public static final int BLUEPRINT_ID = 0;
    public static final int BASE_ID = 1;
    public static final int ADDITION_ID = 2;
    public static final int OUTPUT_ID = 3;

    public static final int BLUEPRINT_X = 35;
    public static final int BASE_X = 53;
    public static final int ADDITION_X = 71;
    public static final int OUTPUT_X = 125;
    public static final int SLOT_Y = 48;

    private final World world;
    private final Property invalidRecipe;

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ORScreenHandlerTypes.FLETCHING, syncId, playerInventory, context, createForgingSlotsManager());
        this.world = playerInventory.player.getEntityWorld();
        this.invalidRecipe = Property.create();
        this.addProperty(this.invalidRecipe).set(0);
    }

    /**
     * 构建 1.21.11 槽位管理器：
     * 全面收紧输入槽位断言，彻底封堵套娃漏洞与非法杂物放置
     */
    private static ForgingSlotsManager createForgingSlotsManager() {
        return ForgingSlotsManager.builder()
                // 槽 0：射艺改装图纸槽 (35, 48) - 仅限改装图纸
                .input(BLUEPRINT_ID, BLUEPRINT_X, SLOT_Y, FletchingScreenHandler::isValidBlueprint)
                // 槽 1：射具底模槽 (53, 48) - 仅限原版弓或弩
                .input(BASE_ID, BASE_X, SLOT_Y, FletchingScreenHandler::isValidBase)
                // 槽 2：材料槽 (71, 48) - 仅限四种法定改装原材料
                .input(ADDITION_ID, ADDITION_X, SLOT_Y, FletchingScreenHandler::isValidAddition)
                // 槽 3：成品输出槽 (125, 48)
                .output(OUTPUT_ID, OUTPUT_X, SLOT_Y)
                .build();
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isOf(Blocks.FLETCHING_TABLE);
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraftByPlayer(player, stack.getCount());
        this.output.unlockLastRecipe(player, this.getInputStacks());

        this.decrementStack(BLUEPRINT_ID);
        this.decrementStack(BASE_ID);
        this.decrementStack(ADDITION_ID);

        this.context.run((w, pos) ->
                w.playSound(null, pos, ORSounds.BLOCK_FLETCHING_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F)
        );
    }

    private List<ItemStack> getInputStacks() {
        return List.of(this.input.getStack(BLUEPRINT_ID), this.input.getStack(BASE_ID), this.input.getStack(ADDITION_ID));
    }

    private FletchingRecipeInput createRecipeInput() {
        return new FletchingRecipeInput(
                this.input.getStack(BLUEPRINT_ID),
                this.input.getStack(BASE_ID),
                this.input.getStack(ADDITION_ID)
        );
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.input.getStack(slot);
        if (!itemStack.isEmpty()) {
            itemStack.decrement(1);
            this.input.setStack(slot, itemStack);
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (this.world instanceof ServerWorld) {
            boolean hasAllInputsNoOutput = this.getSlot(BLUEPRINT_ID).hasStack()
                    && this.getSlot(BASE_ID).hasStack()
                    && this.getSlot(ADDITION_ID).hasStack()
                    && !this.getSlot(this.getResultSlotIndex()).hasStack();
            this.invalidRecipe.set(hasAllInputsNoOutput ? 1 : 0);
        }
    }

    @Override
    public void updateResult() {
        FletchingRecipeInput recipeInput = this.createRecipeInput();
        Optional<RecipeEntry<FletchingRecipe>> optional;

        if (this.world instanceof ServerWorld serverWorld) {
            optional = serverWorld.getRecipeManager().getFirstMatch(ORRecipeTypes.FLETCHING, recipeInput, serverWorld);
        } else {
            optional = Optional.empty();
        }

        optional.ifPresentOrElse(recipe -> {
            ItemStack outputStack = recipe.value().craft(recipeInput, this.world.getRegistryManager());
            this.output.setLastRecipe(recipe);
            this.output.setStack(0, outputStack);
        }, () -> {
            this.output.setLastRecipe(null);
            this.output.setStack(0, ItemStack.EMPTY);
        });
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
    }

    /**
     * 完全同构原版 SmithingScreenHandler.isValidIngredient()：
     * 支撑玩家在背包中 Shift-点击 物品时，精准根据合法材料规则依次自动分发到对应槽位！
     */
    @Override
    public boolean isValidIngredient(ItemStack stack) {
        if (isValidBlueprint(stack) && !this.getSlot(BLUEPRINT_ID).hasStack()) {
            return true;
        } else if (isValidBase(stack) && !this.getSlot(BASE_ID).hasStack()) {
            return true;
        } else {
            return isValidAddition(stack) && !this.getSlot(ADDITION_ID).hasStack();
        }
    }

    public boolean hasInvalidRecipe() {
        return this.invalidRecipe.get() > 0;
    }

    /* 合法材料判定与多重射击检测核心规则。*/
    public static boolean isValidBlueprint(ItemStack stack) {
        return stack.isOf(ORItems.ARCHERY_MODIFICATION_BLUEPRINT);
    }

    public static boolean isValidBase(ItemStack stack) {
        return stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW);
    }

    public static boolean isValidAddition(ItemStack stack) {
        return stack.isOf(Items.BAMBOO)
                || stack.isOf(Items.BREEZE_ROD)
                || stack.isOf(Items.COMPARATOR)
                || stack.isOf(Items.DROPPER);
    }

    public static boolean hasMultishotEnchantment(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            return false;
        }
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            if (entry.matchesKey(Enchantments.MULTISHOT)) {
                return true;
            }
        }
        return false;
    }
}