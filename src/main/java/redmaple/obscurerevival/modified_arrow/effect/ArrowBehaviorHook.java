package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * 箭矢自定义行为钩子接口。
 * <p>
 * 职责：定义箭矢在完整生命周期（初始化、飞行、命中、销毁）中的事件钩子。
 * 允许特异效果注入粒子、范围伤害、状态效果等非物理数值的业务逻辑。
 */
public interface ArrowBehaviorHook {

    default void onInit(PersistentProjectileEntity arrow) {}

    default void onTick(PersistentProjectileEntity arrow) {}

    default boolean onEntityHit(PersistentProjectileEntity arrow, EntityHitResult hitResult) { return false; }

    default void onBlockHit(PersistentProjectileEntity arrow, HitResult hitResult) {}

    default void onRemoved(PersistentProjectileEntity arrow) {}
}
