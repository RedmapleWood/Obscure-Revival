package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * 末影之眼特异属性行为实现类。
 * <p>
 * 行为逻辑：
 * <li>1. 增益：发射时采用高斯正态分布（Gaussian Distribution）替代原版三角分布，大幅压制弹道离散；
 * <li>2. 减益资格：仅当满弓/满弩拉满 (isCritical) 且命中 20% 概率时，箭矢被打上“破碎宿命标签”；
 * <li>3. 减益爆裂：宿命箭矢在飞行超过 35 tick 安全期后，每 tick 独立进行 1.2% 概率破碎检定；着陆方块时必定粉碎。
 */
public class AccuracyBoostBehavior implements SpecialEffectBehavior {

    private final Map<PersistentProjectileEntity, ArrowState> arrowStates = new WeakHashMap<>();
    private final Random random = new Random();

    @Override
    public Vec3d overrideVelocity(PersistentProjectileEntity arrow, double x, double y, double z, float power, float uncertainty) {
        // 增益逻辑：利用高斯分布重新构建初速度向量（标准差缩放系数定稿为 0.005）
        double spread = uncertainty * 0.005;
        double offsetX = this.random.nextGaussian() * spread;
        double offsetY = this.random.nextGaussian() * spread;
        double offsetZ = this.random.nextGaussian() * spread;
        return new Vec3d(x, y, z).normalize().add(offsetX, offsetY, offsetZ).multiply(power);
    }

    @Override
    public void onTick(PersistentProjectileEntity arrow) {
        // 1. 客户端视觉表现：末影传送门粒子尾迹
        if (arrow.getEntityWorld().isClient()) { this.spawnEnderParticles(arrow); return; }

        // 2. 服务端减益逻辑
        ArrowState state = this.arrowStates.computeIfAbsent(arrow, a -> new ArrowState());

        // 阶段一：宿命资格判定（仅在发射后第 1 个 tick 执行一次，满弓拉满且 20% 概率抽中）
        if (!state.decided) {
            state.decided = true;
            if (arrow.isCritical() && arrow.getEntityWorld().getRandom().nextFloat() < 0.2F) {
                state.doomedToShatter = true;
            }
        }

        // 阶段二：超过 35 tick 安全期后，每 tick 独立进行 0.01255F 概率的破碎检定
        if (state.doomedToShatter) {
            state.airTicks++;

            if (state.airTicks > 35) {
                if (arrow.getEntityWorld().getRandom().nextFloat() < 0.01255F) { this.shatter(arrow); }
            }
        }
    }

    @Override
    public void onBlockHit(PersistentProjectileEntity arrow, HitResult hitResult) {
        // 保底机制：宿命箭矢在着陆方块的一瞬间必定破碎
        if (!arrow.getEntityWorld().isClient()) {
            ArrowState state = this.arrowStates.get(arrow);
            if (state != null && state.doomedToShatter) { this.shatter(arrow); }
        }
    }

    @Override
    public void onRemoved(PersistentProjectileEntity arrow) { this.arrowStates.remove(arrow); }

    /** 触发末影之眼碎裂声效、2003 粒子事件并销毁实体 */
    private void shatter(PersistentProjectileEntity arrow) {
        if (arrow.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(
                    null,
                    arrow.getX(), arrow.getY(), arrow.getZ(),
                    SoundEvents.ENTITY_ENDER_EYE_DEATH,
                    SoundCategory.NEUTRAL,
                    1.0F, 1.0F
            );
            serverWorld.syncWorldEvent(2003, arrow.getBlockPos(), 0);
        }
        arrow.discard();
    }

    /** 客户端生成末影粒子 */
    private void spawnEnderParticles(PersistentProjectileEntity arrow) {
        Vec3d vel = arrow.getVelocity();
        arrow.getEntityWorld().addParticleClient(
                ParticleTypes.PORTAL,
                arrow.getX() + (arrow.getRandom().nextDouble() - 0.5) * 0.2,
                arrow.getY() + (arrow.getRandom().nextDouble() - 0.5) * 0.2,
                arrow.getZ() + (arrow.getRandom().nextDouble() - 0.5) * 0.2,
                -vel.x * 0.2,
                -vel.y * 0.2,
                -vel.z * 0.2
        );
    }

    private static class ArrowState { boolean decided = false; boolean doomedToShatter = false; int airTicks = 0; }
}
