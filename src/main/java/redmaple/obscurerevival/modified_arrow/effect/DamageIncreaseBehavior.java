package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.LivingEntity;

/**
 * 铁锭特异属性行为实现类。
 * <p>行为逻辑：每次命中时在基础伤害上额外增加 0.44。
 */
public class DamageIncreaseBehavior implements SpecialEffectBehavior {
    @Override
    public double modifyDamage(double originalDamage, LivingEntity target) { return originalDamage + 0.44; }
}