/*
Obscure Revival © 2026 by Redmaple Wood is licensed under CC BY-NC-SA 4.0.
To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
(Optional but appreciated: If you include this mod in a modpack, consider letting me know!)
*/

package redmaple.obscurerevival.refined_chain_armor;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import redmaple.obscurerevival.attribute.CustomizedAttributes;

public class KeloidEnchantmentManager {

    private static final Identifier KELOID_ARMOR_MODIFIER_ID =
            Identifier.of("obscure_revival", "keloid_armor_boost");

    /**
     * 处理 3 秒倒计时与护甲回落，同时校验装备件数上限（由 Mixin 的 tick 触发）
     */
    public static void handleTick(LivingEntity entity) {
        if (!(entity instanceof KeloidStateAccess state)) return;

        // 从属性直接读取当前层数（护甲值/2），KELOID_ARMOR 是唯一数据源
        int currentStep = getKeloidCurrentStep(entity);

        // 每 tick 校验层数是否超出当前装备件数上限
        int enchantedPieces = getKeloidEnchantedPiecesCount(entity);
        if (currentStep > enchantedPieces) {
            currentStep = enchantedPieces;
            state.obscure_revival$setKeloidFailCount(0);
            updateKeloidArmor(entity, currentStep * 2);
        }

        // timer 倒计时，归零时应激平息
        int timer = state.obscure_revival$getKeloidTimer();
        if (timer > 0) {
            timer--;
            state.obscure_revival$setKeloidTimer(timer);
            if (timer == 0) {
                state.obscure_revival$setKeloidFailCount(0);
                updateKeloidArmor(entity, 0);
            }
        }
    }

    /**
     * 处理受伤时的护甲动态成长（由 ObscureRevival 的 AFTER_DAMAGE 事件触发）
     */
    public static void handleDamage(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof KeloidStateAccess state)) return;
        if (source.getAttacker() != null && source.getAttacker() == source.getSource()) {
            int enchantedPieces = getKeloidEnchantedPiecesCount(entity);
            if (enchantedPieces > 0) {
                // 【防断连机制】只要挨了近战打，强行重置 3 秒倒计时
                state.obscure_revival$setKeloidTimer(60);

                // 从属性直接读取当前层数
                int currentStep = getKeloidCurrentStep(entity);

                // 层数未达上限时进行 PRD 概率判定
                if (currentStep < enchantedPieces) {
                    int failCount = state.obscure_revival$getKeloidFailCount();
                    // PRD 核心公式：基础 30.2%，每次失败叠加 15%，期望触发率 50%
                    float currentChance = 0.302f + (failCount * 0.15f);
                    if (entity.getRandom().nextFloat() < currentChance) {
                        currentStep++;
                        state.obscure_revival$setKeloidFailCount(0);
                        updateKeloidArmor(entity, currentStep * 2);
                    } else {
                        state.obscure_revival$setKeloidFailCount(failCount + 1);
                    }
                }
            }
        }
    }

    /**
     * 从 KELOID_ARMOR 自定义属性读取当前层数（护甲值/2）
     * KELOID_ARMOR 是层数的唯一数据源，不再用 KeloidStateAccess 字段冗余存储
     */
    private static int getKeloidCurrentStep(LivingEntity entity) {
        EntityAttributeInstance keloidInstance =
                entity.getAttributeInstance(CustomizedAttributes.KELOID_ARMOR);
        if (keloidInstance == null) return 0;
        return (int) (keloidInstance.getValue() / 2);
    }

    /**
     * 统计身上带有战痕应激附魔的锁链甲数量
     */
    private static int getKeloidEnchantedPiecesCount(LivingEntity entity) {
        var enchantmentRegistry = entity.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var keloidEntry = enchantmentRegistry.getOptional(RCAEnchantmentsRegistry.KELOID).orElse(null);
        if (keloidEntry == null) return 0;
        int count = 0;
        ItemStack[] stacks = {
                entity.getEquippedStack(EquipmentSlot.HEAD),
                entity.getEquippedStack(EquipmentSlot.CHEST),
                entity.getEquippedStack(EquipmentSlot.LEGS),
                entity.getEquippedStack(EquipmentSlot.FEET)
        };
        for (ItemStack stack : stacks) {
            if (!stack.isOf(Items.AIR) &&
                    stack.isOf(Items.CHAINMAIL_HELMET) ||
                    stack.isOf(Items.CHAINMAIL_CHESTPLATE) ||
                    stack.isOf(Items.CHAINMAIL_LEGGINGS) ||
                    stack.isOf(Items.CHAINMAIL_BOOTS)) {
                if (EnchantmentHelper.getLevel(keloidEntry, stack) > 0) count++;
            }
        }
        return count;
    }

    /**
     * 同步更新两处护甲值：
     * 1. KELOID_ARMOR 自定义属性（setTracked(true) 自动同步到客户端，供 HUD 渲染读取）
     * 2. 原版 ARMOR 属性上的修饰符（保证实际减伤计算正确）
     */
    private static void updateKeloidArmor(LivingEntity entity, double bonusValue) {
        EntityAttributeInstance keloidInstance =
                entity.getAttributeInstance(CustomizedAttributes.KELOID_ARMOR);
        if (keloidInstance != null) {
            keloidInstance.removeModifier(KELOID_ARMOR_MODIFIER_ID);
            if (bonusValue > 0) {
                keloidInstance.addTemporaryModifier(new EntityAttributeModifier(
                        KELOID_ARMOR_MODIFIER_ID,
                        bonusValue,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        EntityAttributeInstance armorInstance =
                entity.getAttributeInstance(EntityAttributes.ARMOR);
        if (armorInstance != null) {
            armorInstance.removeModifier(KELOID_ARMOR_MODIFIER_ID);
            if (bonusValue > 0) {
                armorInstance.addTemporaryModifier(new EntityAttributeModifier(
                        KELOID_ARMOR_MODIFIER_ID,
                        bonusValue,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }
}