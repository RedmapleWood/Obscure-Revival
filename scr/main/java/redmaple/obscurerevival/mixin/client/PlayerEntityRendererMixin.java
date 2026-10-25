package redmaple.obscurerevival.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import redmaple.obscurerevival.client.render.item.RangedWeaponClientHelper;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    /** 拦截 getArmPose 中对 stack.isOf(Items.CROSSBOW) 的判定，兼容 Mod 弩并唤醒 ArmPose.CROSSBOW_HOLD（双手持弩瞄准姿态）。*/
    @ModifyExpressionValue(
            method = "getArmPose(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean obscurerevival$supportModCrossbowHoldPose(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || RangedWeaponClientHelper.isCrossbow(stack);
    }
}