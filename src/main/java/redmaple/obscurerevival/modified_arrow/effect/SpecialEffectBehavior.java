package redmaple.obscurerevival.modified_arrow.effect;

/**
 * 特异效果行为总契约。
 * <p>
 * 职责：组合物理属性修改与行为钩子接口，作为策略枚举统一持有的行为基类。
 */
public interface SpecialEffectBehavior extends ArrowPhysicsModifier, ArrowBehaviorHook {}
