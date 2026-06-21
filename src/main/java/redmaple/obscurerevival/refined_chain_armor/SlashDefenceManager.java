package redmaple.obscurerevival.refined_chain_armor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

public class SlashDefenceManager {
    /**
     *计算锁链甲对劈砍伤害的特化减免
     *@param victim 受击者
     *@param source 伤害来源
     *@param originalDamage 经过原版护甲和保护附魔削弱后的剩余伤害
     *@return 最终受到的伤害
     */
    public static float applySlashingDefense(LivingEntity victim, DamageSource source, float originalDamage) {
        //1.必须是实体近战攻击
        Entity attacker = source.getAttacker();
        if (!(attacker instanceof LivingEntity livingAttacker) || source.getAttacker() != source.getSource()) return originalDamage;

        //2.识别攻击者主手中的武器类型 (在1.21左右的版本中，因为物品系统被进行了深度的数据“驱动化”与“组件化”重构，应使用ItemTags替代instanceof)
        ItemStack weaponStack = livingAttacker.getMainHandStack();
        boolean isSword = weaponStack.isIn(ItemTags.SWORDS);
        boolean isAxe = weaponStack.isIn(ItemTags.AXES);
        if (!isSword && !isAxe) return originalDamage;//如果既不是剑也不是斧，直接返回原伤害

        //3.统计受击者身上穿戴的锁链甲件数
        int chainPieces = 0;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = victim.getEquippedStack(slot);
            if (!stack.isEmpty() && (
                    stack.isOf(Items.CHAINMAIL_HELMET) ||
                            stack.isOf(Items.CHAINMAIL_CHESTPLATE) ||
                            stack.isOf(Items.CHAINMAIL_LEGGINGS) ||
                            stack.isOf(Items.CHAINMAIL_BOOTS))) { chainPieces++; }
        }
        if (chainPieces == 0) return originalDamage;

        //4.计算减伤比例 (剑: 每件16.25%; 斧: 每件12.5%)
        float reductionPerPiece = isSword ? 0.1625f : 0.125f;
        float totalReduction = chainPieces * reductionPerPiece;

        //5.结算最终伤害
        float finalDamage = originalDamage * (1.0f - totalReduction);
        return Math.max(0.0f, finalDamage);
    }
}
