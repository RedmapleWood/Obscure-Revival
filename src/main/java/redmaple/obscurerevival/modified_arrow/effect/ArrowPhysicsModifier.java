package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * 箭矢物理属性修改接口。
 * <p>
 * 职责：定义对箭矢基础物理属性（如速度、精准度、重力、阻力、伤害）的修饰契约。
 * 允许特异效果在不改变原版底层逻辑的前提下，动态干预物理结算。
 */
public interface ArrowPhysicsModifier {

    default float modifyLaunchPower(float originalPower) { return originalPower; }

    default float modifyLaunchUncertainty(float originalUncertainty) { return originalUncertainty; }

    @Nullable
    default Vec3d overrideVelocity(PersistentProjectileEntity arrow, double x, double y, double z, float power, float uncertainty) {
        return null;
    }

    @Nullable default Float overrideDragInWater() { return null; }

    @Nullable default Float overrideDrag() { return null; }

    @Nullable default Double overrideGravity() { return null; }

    default double modifyDamage(double originalDamage, LivingEntity target) { return originalDamage; }
}
