package redmaple.obscurerevival.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import redmaple.obscurerevival.client.render.item.RangedWeaponClientHelper;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    /** 拦截 getFovMultiplier 中 this.getActiveItem().isOf(Items.BOW) 的布尔值，若原版返回 false，则交由辅助方法判断是否为 Mod 弓。*/
    @ModifyExpressionValue(
            method = "getFovMultiplier",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean obscurerevival$supportModBowsFov(boolean original) {
        if (original) return true;
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        ItemStack activeItem = player.getActiveItem();
        return RangedWeaponClientHelper.isBow(activeItem);
    }

    /** 拦截原版写死的除数 20.0F，根据手持物品动态替换为风弓的 14.0F 或原版的 20.0F。*/
    @ModifyExpressionValue(
            method = "getFovMultiplier",
            at = @At(value = "CONSTANT", args = "floatValue=20.0F")
    )
    private float obscurerevival$dynamicBowPullTicks(float original) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        ItemStack activeItem = player.getActiveItem();
        return RangedWeaponClientHelper.getBowPullTicks(activeItem);
    }
}