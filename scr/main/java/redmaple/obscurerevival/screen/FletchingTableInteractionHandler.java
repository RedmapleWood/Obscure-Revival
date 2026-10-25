package redmaple.obscurerevival.screen;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class FletchingTableInteractionHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND) { return ActionResult.PASS; } // 只在主手阶段处理，避免双臂重复触发
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.FLETCHING_TABLE)) { return ActionResult.PASS; } // 过滤非制箭台方块
            if (player.isSpectator()) { return ActionResult.PASS; }// 旁观者模式不开启界面
            /* 潜行且手中持物时优先响应物品自身行为（如放方块）*/
            boolean hasItem = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            if (player.shouldCancelInteraction() && hasItem) { return ActionResult.PASS; }

            if (!world.isClient()) {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, p) ->
                                new FletchingScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, hitResult.getBlockPos())),
                        FletchingScreenHandler.TITLE
                ));
            }

            return ActionResult.SUCCESS;
        });
    }

    private FletchingTableInteractionHandler() {}
}