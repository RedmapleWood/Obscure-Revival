package redmaple.obscurerevival.fletching_table.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;

import redmaple.obscurerevival.vanilla_registry.Sounds;

/**
 * 制箭台成品输出槽位。
 * <p>
 * 职责：作为单向输出槽位禁止手动塞入物品，并在玩家取出成品时，
 * 负责触发输入背包材料的原子化扣减、原版级 Tick 节流音效播放与合成状态刷新的链式反应。
 */
public class FletchingOutputSlot extends Slot {

    private final FletchingTableScreenHandler handler;
    private final Inventory inputInventory;
    private final ScreenHandlerContext context;

    public FletchingOutputSlot(FletchingTableScreenHandler handler, Inventory inputInventory, Inventory resultInventory, ScreenHandlerContext context, int index, int x, int y) {
        super(resultInventory, index, x, y);
        this.handler = handler;
        this.inputInventory = inputInventory;
        this.context = context;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        // 1. 原子化扣减输入背包材料
        for (int i = 0; i < 4; i++) {
            ItemStack inputStack = this.inputInventory.getStack(i);
            if (!inputStack.isEmpty()) {
                inputStack.decrement(1);
            }
        }

        // 2. 模仿原版机制：在方块精准坐标处播放音效，并加入同 Tick 防重叠节流与 Pitch 随机微调
        this.context.run((world, pos) -> {
            long currentTime = world.getTime();
            if (this.handler.getLastTakeResultTime() != currentTime) {
                float pitch = 0.95F + player.getRandom().nextFloat() * 0.1F;
                world.playSound(null, pos, Sounds.UI_FLETCHING_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, pitch);
                this.handler.setLastTakeResultTime(currentTime);
            }
        });

        // 3. 触发主容器 onContentChanged -> 重新计算并刷新预览
        this.inputInventory.markDirty();
        super.onTakeItem(player, stack);
    }
}