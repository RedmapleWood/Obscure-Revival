package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 幻翼膜特异属性行为实现类。
 * <p>
 * 行为逻辑：
 * <li>增益：将重力加速度由原版 0.05 降低至 0.0025（下坠减缓 95%），实现近乎水平的滑翔漂浮弹道；
 * <li>性能保底：对极低重力箭矢执行 400 tick 飞行超时销毁，以及离开加载区块/发射者失效时的即时销毁。
 */
public class LightBehavior implements SpecialEffectBehavior {

    // 单独记录每支箭矢在空中的飞行时间
    private final Map<PersistentProjectileEntity, ArrowState> arrowStates = new WeakHashMap<>();

    @Override
    public Double overrideGravity() {
        // 增益：将重力锁定为 0.0025，实现极低重力平直滑翔
        return 0.0025;
    }

    @Override
    public void onTick(PersistentProjectileEntity arrow) {
        // 1. 客户端视觉表现：幻翼灰烟与菌丝微粒尾迹
        if (arrow.getEntityWorld().isClient()) {
            this.spawnPhantomParticles(arrow);
            return;
        }

        // 2. 服务端关键保底机制：检查发射者有效性与所在位置区块是否已加载
        Entity owner = arrow.getOwner();
        boolean isOwnerValid = (owner == null || !owner.isRemoved());
        @SuppressWarnings("deprecation")// WorldView接口的isChunkLoaded(BlockPos pos)已被弃用
        boolean isChunkLoaded = arrow.getEntityWorld().isChunkLoaded(arrow.getBlockPos());

        // 若发射者已销毁或箭矢飞入未加载区块，立刻销毁，阻断无效物理与内存开销
        if (!isOwnerValid || !isChunkLoaded) {
            arrow.discard();
            this.arrowStates.remove(arrow);
            return;
        }

        // 3. 服务端飞行寿命保底计数
        ArrowState state = this.arrowStates.computeIfAbsent(arrow, a -> new ArrowState());
        state.lifespan++;

        // 超过 400 tick（20秒）强制销毁实体，防止极低重力箭矢无限游荡
        if (state.lifespan > 400) {
            arrow.discard();
            this.arrowStates.remove(arrow);
        }
    }

    @Override
    public void onRemoved(PersistentProjectileEntity arrow) {
        this.arrowStates.remove(arrow);
    }

    /** 客户端生成幻翼双翅灰烟与菌丝微粒 */
    private void spawnPhantomParticles(PersistentProjectileEntity arrow) {
        Vec3d vel = arrow.getVelocity();
        double spread = 0.15;

        // 幻翼双翅灰烟粒子
        arrow.getEntityWorld().addParticleClient(
                ParticleTypes.SMOKE,
                arrow.getX() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                arrow.getY() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                arrow.getZ() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                -vel.x * 0.05,
                -vel.y * 0.05 + 0.01,
                -vel.z * 0.05
        );

        // 幻翼同款绿色菌丝微粒点缀
        arrow.getEntityWorld().addParticleClient(
                ParticleTypes.MYCELIUM,
                arrow.getX() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                arrow.getY() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                arrow.getZ() + (arrow.getRandom().nextDouble() - 0.5) * spread,
                0.0, 0.0, 0.0
        );
    }

    private static class ArrowState { int lifespan = 0; }
}
