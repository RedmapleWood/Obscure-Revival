package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.math.MathHelper;
import redmaple.obscurerevival.ObscureRevivalComponents;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowComponent;

/**
 * 黑曜石特异属性行为实现类（破甲箭）。
 * <p>行为逻辑：
 * <li>增益：命中伤害固定提升 +2.0；100% 复刻原版破甲（Breach）附魔算法，削减目标 70% 护甲减伤率。
 * <li>减益：完全禁用弓类武器射击；弩射出后强制进入 10 秒冷却。
 */
public class ArmorPierceBehavior implements SpecialEffectBehavior {

    @Override
    public double modifyDamage(double originalDamage, LivingEntity target) { return originalDamage + 2.0; }

    /** 破甲箭专用护甲结算：与原版 Breach 附魔同形态 */
    public static float computePiercedArmorOutput(float amount, float armor, float toughness) {
        if (armor <= 0.0F) {
            return amount;
        }
        float h = reductionFactor(amount, armor, toughness);
        float hPierced = h * 0.3F;
        return amount * (1.0F - hPierced);
    }

    /** 复刻 DamageUtil.getDamageLeft 的原版减伤率 h（不含附魔护甲有效性步骤） */
    private static float reductionFactor(float amount, float armor, float toughness) {
        float f = 2.0F + toughness / 4.0F;
        float g = MathHelper.clamp(armor - amount / f, armor * 0.2F, 20.0F);
        return g / 25.0F;
    }

    /** 判断物品堆是否为带破甲效果的黑曜石改装箭（只匹配 ARMOR_PIERCE，不影响其他特异箭） */
    public static boolean isPiercingArrow(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ModifiedArrowComponent component = stack.get(ObscureRevivalComponents.ARROW_STATS);
        return component != null && component.specialEffects().contains(ArrowSpecialEffect.ARMOR_PIERCE);
    }

    /**
     * 弓侧完全阻断：玩家用弓射击时，若【副手/手持】弹药为破甲箭则阻断整个射击流程。
     * 与 {@code UnderwaterNoDragBehavior.shouldBlockHeld} 同模式（在 getProjectileType HEAD 调用）。
     * 快捷栏/背包的跳过由 {@link #shouldSkipInventory} 补齐，两路结合使弓对破甲箭完全失效。
     */
    public static boolean blockPiercingBowShot(PlayerEntity player, ItemStack weaponStack) {
        if (!(weaponStack.getItem() instanceof BowItem bow)) {
            return false; // 弩等武器不阻断，正常检测
        }
        return isPiercingArrow(RangedWeaponItem.getHeldProjectile(player, bow.getHeldProjectiles()));
    }

    /**
     * 物品栏/背包自动选弹跳过破甲箭（仅弓生效，弩正常检测）。
     * @param ammo   遍历中正在测试的弹药
     * @param weapon 本次 getProjectileType 的武器（getProjectileType 首参 weaponStack）
     */
    public static boolean shouldSkipInventory(ItemStack ammo, ItemStack weapon) {
        return weapon.getItem() instanceof BowItem && isPiercingArrow(ammo);
    }
}
