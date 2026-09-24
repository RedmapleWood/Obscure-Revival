package redmaple.obscurerevival.fletching_table.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import redmaple.obscurerevival.fletching_table.FletchingCraftingEngine;
import redmaple.obscurerevival.fletching_table.FletchingTableScreenHandlers;
import redmaple.obscurerevival.modified_arrow.material.ArrowMaterialRegistry;

import java.util.Optional;

/**
 * 制箭台 GUI 逻辑处理器 (ScreenHandler)。
 * <p>
 * 职责：作为双端通用的容器控制核心，采用双背包模式管理材料输入与虚拟预览输出，
 * 负责处理 UI 布局、快速转移路由、音效节流时间戳记录以及委托 FletchingCraftingEngine 执行合成评估。
 */
public class FletchingTableScreenHandler extends ScreenHandler {
    public static final int ARROW_SLOT = 0;
    public static final int ARROWHEAD_SLOT = 1;
    public static final int SHAFT_SLOT = 2;
    public static final int FLETCH_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;

    private static final int INPUT_START = 0;
    private static final int INPUT_END = 4;
    private static final int INVENTORY_START = 5;
    private static final int INVENTORY_END = 32;
    private static final int HOTBAR_START = 32;
    private static final int HOTBAR_END = 41;

    private final Inventory input = new SimpleInventory(4) {
        @Override
        public void markDirty() {
            super.markDirty();
            FletchingTableScreenHandler.this.onContentChanged(this);
        }
    };

    private final Inventory result = new CraftingResultInventory();
    private final ScreenHandlerContext context;

    private long lastTakeResultTime;// 记录上一次播放合成音效的游戏时间戳 (用于防重叠音爆节流)

    public FletchingTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public FletchingTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(FletchingTableScreenHandlers.FLETCHING_TABLE, syncId);
        this.context = context;

        this.addSlot(new GhostIconSlot(this.input, ARROW_SLOT, 38, 35,
                Identifier.of("obscure_revival", "container/slot/arrow"),
                () -> true,
                stack -> stack.isOf(Items.ARROW)));

        this.addSlot(new GhostIconSlot(this.input, ARROWHEAD_SLOT, 74, 17,
                Identifier.of("obscure_revival", "container/slot/arrowhead"),
                () -> !this.input.getStack(ARROW_SLOT).isEmpty(),
                stack -> ArrowMaterialRegistry.ARROWHEAD_EFFECTS.containsKey(stack.getItem())));

        this.addSlot(new GhostIconSlot(this.input, SHAFT_SLOT, 74, 35,
                Identifier.of("obscure_revival", "container/slot/shaft"),
                () -> !this.input.getStack(ARROW_SLOT).isEmpty(),
                stack -> ArrowMaterialRegistry.SHAFT_EFFECTS.containsKey(stack.getItem())));

        this.addSlot(new GhostIconSlot(this.input, FLETCH_SLOT, 74, 53,
                Identifier.of("obscure_revival", "container/slot/fletch"),
                () -> !this.input.getStack(ARROW_SLOT).isEmpty(),
                stack -> ArrowMaterialRegistry.FLETCH_EFFECTS.containsKey(stack.getItem())));

        // 构建输出槽位：传入 Handler 引用与 ScreenHandlerContext
        this.addSlot(new FletchingOutputSlot(this, this.input, this.result, this.context, 0, 134, 35));
        this.addPlayerSlots(playerInventory, 8, 84);
    }

    public long getLastTakeResultTime() {
        return this.lastTakeResultTime;
    }

    public void setLastTakeResultTime(long lastTakeResultTime) {
        this.lastTakeResultTime = lastTakeResultTime;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Blocks.FLETCHING_TABLE);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == OUTPUT_SLOT && actionType == SlotActionType.PICKUP_ALL) {
            actionType = SlotActionType.PICKUP;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            this.updateOutputPreview();
        }
    }

    private void updateOutputPreview() {
        Optional<ItemStack> previewResult = FletchingCraftingEngine.craft(this.input);
        this.result.setStack(0, previewResult.orElse(ItemStack.EMPTY));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            resultStack = stackInSlot.copy();

            if (slotIndex == OUTPUT_SLOT) {
                while (slot.hasStack()) {
                    ItemStack previewStack = slot.getStack();
                    ItemStack copyToTransfer = previewStack.copy();

                    if (!this.insertItem(previewStack, INVENTORY_START, HOTBAR_END, true)) {
                        return ItemStack.EMPTY;
                    }

                    slot.onQuickTransfer(previewStack, copyToTransfer);
                    slot.onTakeItem(player, previewStack);

                    if (copyToTransfer.getCount() == previewStack.getCount()) {
                        break;
                    }
                }
                return ItemStack.EMPTY;
            } else if (slotIndex >= INPUT_START && slotIndex < INPUT_END) {
                if (!this.insertItem(stackInSlot, INVENTORY_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.isOf(Items.ARROW)) {
                    if (!this.insertItem(stackInSlot, ARROW_SLOT, ARROW_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (ArrowMaterialRegistry.ARROWHEAD_EFFECTS.containsKey(stackInSlot.getItem())) {
                    if (!this.insertItem(stackInSlot, ARROWHEAD_SLOT, ARROWHEAD_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (ArrowMaterialRegistry.SHAFT_EFFECTS.containsKey(stackInSlot.getItem())) {
                    if (!this.insertItem(stackInSlot, SHAFT_SLOT, SHAFT_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (ArrowMaterialRegistry.FLETCH_EFFECTS.containsKey(stackInSlot.getItem())) {
                    if (!this.insertItem(stackInSlot, FLETCH_SLOT, FLETCH_SLOT + 1, false)) return ItemStack.EMPTY;
                } else {
                    if (slotIndex < HOTBAR_START) {
                        if (!this.insertItem(stackInSlot, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
                    } else {
                        if (!this.insertItem(stackInSlot, INVENTORY_START, INVENTORY_END, false)) return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stackInSlot.getCount() == resultStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stackInSlot);
        }
        return resultStack;
    }
}
