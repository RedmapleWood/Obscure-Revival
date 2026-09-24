package redmaple.obscurerevival.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowPierceArrowMixin {

    /** 在 use 头部注入，本方法在其进入装填或发射流程前实施封锁，强制生效 10 秒硬性冷却 */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void obscure_revival$blockCrossbowUseWhileCooling(
            World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack crossbowStack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(crossbowStack)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
