package redmaple.obscurerevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redmaple.obscurerevival.modified_arrow.effect.ArmorPierceBehavior;
import redmaple.obscurerevival.modified_arrow.effect.UnderwaterNoDragBehavior;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * 手持弹药阻断检查：海晶箭（干燥）+ 破甲箭（弓）均阻断射击流程。
     * <p>破甲箭仅当武器为弓时阻断，弩不受影响。
     */
    @Inject(method = "getProjectileType", at = @At("HEAD"), cancellable = true)
    private void obscure_revival$forwardHeldBlockCheck(ItemStack weaponStack, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (UnderwaterNoDragBehavior.shouldBlockHeld(self, weaponStack)
                || ArmorPierceBehavior.blockPiercingBowShot(self, weaponStack)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /**
     * 背包槽位遍历：海晶箭（干燥）跳过（既有逻辑不动）；破甲箭仅弓跳过、弩正常检测。
     * <p>通过 @Local 捕获 getProjectileType 的首参 weaponStack 以区分弓/弩。
     */
    @WrapOperation(
            method = "getProjectileType",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z")
    )
    private boolean obscure_revival$forwardInventorySkipCheck(
            Predicate<ItemStack> predicate, Object itemStackObj, Operation<Boolean> original,
            @Local(argsOnly = true, ordinal = 0) ItemStack weaponStack) {
        if (!original.call(predicate, itemStackObj)) { return false; }

        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack ammo = (ItemStack) itemStackObj;
        if (UnderwaterNoDragBehavior.shouldSkipInventory(self, ammo)) { return false; }
        if (ArmorPierceBehavior.shouldSkipInventory(ammo, weaponStack)) { return false; }
        return true;
    }
}