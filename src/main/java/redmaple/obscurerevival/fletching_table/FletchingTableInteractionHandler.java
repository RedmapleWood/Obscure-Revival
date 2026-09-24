package redmaple.obscurerevival.fletching_table;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import redmaple.obscurerevival.fletching_table.screen.FletchingTableScreenHandler;

public class FletchingTableInteractionHandler {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // 只在主手触发阶段处理，避免副手重复触发
            if (hand != Hand.MAIN_HAND) { return ActionResult.PASS; }
            // 不是制箭台就完全不插手，交还给游戏正常处理
            if (world.getBlockState(hitResult.getBlockPos()).getBlock() != Blocks.FLETCHING_TABLE) { return ActionResult.PASS; }
            // 手动补上文档提醒的旁观者检查
            if (player.isSpectator()) { return ActionResult.PASS; }
            // 检查主手和副手是否至少有一个不是空的
            boolean hasItem = !player.getStackInHand(Hand.MAIN_HAND).isEmpty() || !player.getStackInHand(Hand.OFF_HAND).isEmpty();
            // 只有当“在潜行”且“手里有东西”时，才放弃交互（让原版去处理放方块或用物品）
            if (player.shouldCancelInteraction() && hasItem) { return ActionResult.PASS; }
            // 模仿保底机制：如果是空手潜行，或者根本没潜行，都会顺利走到这里打开 GUI
            if (!world.isClient()) {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) ->
                                new FletchingTableScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, hitResult.getBlockPos())),
                        Text.translatable("container.obscure_revival.fletching_table")
                ));
            }
            return ActionResult.SUCCESS;
        });
    }
}
