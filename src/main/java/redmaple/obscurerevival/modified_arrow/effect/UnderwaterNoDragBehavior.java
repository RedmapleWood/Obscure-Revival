package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import redmaple.obscurerevival.ObscureRevivalComponents;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowComponent;

/**
 * 海晶碎片特异属性行为实现类。
 * <p>行为逻辑：
 * <li>增益：将水下阻力系数由原版的 0.6F 提升至 0.9F；
 * <li>减益：副手拿干燥海晶箭时，裁决阻断整把武器的使用；背包遍历遇到干燥海晶箭时，裁决跳过当前槽位。
 */
public class UnderwaterNoDragBehavior implements SpecialEffectBehavior {

    @Override
    public Float overrideDragInWater() { return 0.9F; }

    /** 若玩家明确把海晶箭拿在副手，且身体干燥，判定阻断整个射击流程（拒绝拉弓） */
    public static boolean shouldBlockHeld(PlayerEntity player, ItemStack weaponStack) {
        // 若人在水下、雨中，或武器不是远程武器，无需拦截
        if (player.isTouchingWaterOrRain() || !(weaponStack.getItem() instanceof RangedWeaponItem rangedWeapon)) {
            return false;
        }

        // 检索玩家当前副手/手持的弹药
        ItemStack heldAmmo = RangedWeaponItem.getHeldProjectile(player, rangedWeapon.getHeldProjectiles());
        return isUnderwaterArrow(heldAmmo);
    }

    /** 若在背包循环中遇到海晶箭，且玩家身体干燥，判定跳过当前槽位 */
    public static boolean shouldSkipInventory(PlayerEntity player, ItemStack ammo) {
        if (player.isTouchingWaterOrRain() || ammo.isEmpty()) {
            return false;
        }
        return isUnderwaterArrow(ammo);
    }

    /** 私有辅助：检查物品是否带有本效果 */
    private static boolean isUnderwaterArrow(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ModifiedArrowComponent component = stack.get(ObscureRevivalComponents.ARROW_STATS);
        return component != null && component.specialEffects().contains(ArrowSpecialEffect.UNDERWATER_NO_DRAG);
    }
}
