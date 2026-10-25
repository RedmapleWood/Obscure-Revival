package redmaple.obscurerevival.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import redmaple.obscurerevival.client.render.item.RangedWeaponClientHelper;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    /** 泛化 isChargedCrossbow 私有判断方法。*/
    @ModifyExpressionValue(
            method = "isChargedCrossbow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean obscurerevival$isChargedCrossbow(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || RangedWeaponClientHelper.isCrossbow(stack);
    }

    /** 泛化 renderFirstPersonItem 中对 CROSSBOW 渲染分支的硬编码检查。*/
    @ModifyExpressionValue(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean obscurerevival$renderModCrossbowFirstPerson(boolean original, @Local(argsOnly = true) ItemStack item) {
        return original || RangedWeaponClientHelper.isCrossbow(item);
    }
}