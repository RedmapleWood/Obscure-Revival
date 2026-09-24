package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;

/**
 * 竹子特异属性行为实现类。
 * <p>
 * 行为逻辑：
 * <li>增益：通过提升发射初速度模长（1.2倍）与降低空气阻力衰减（0.995F），实现中远距离平直、高速且符合直觉的远射弹道；
 * <li>减益：满弓拉满 (isCritical) 且击中方块时，箭矢粉碎并播放竹子破坏音效与粒子，无法回收。
 */
public class DragReduceBehavior implements SpecialEffectBehavior {

    @Override
    public float modifyLaunchPower(float originalPower) { return originalPower * 1.2F; }

    @Override
    public Float overrideDrag() { return 0.995F; }

    @Override
    public void onBlockHit(PersistentProjectileEntity arrow, HitResult hitResult) {
        // 减益逻辑：仅当满弓拉满 (isCritical) 且射到方块时触发粉碎
        if (arrow.isCritical()) {
            if (arrow.getEntityWorld() instanceof ServerWorld serverWorld) {
                // 1. 播放竹子破坏音效
                serverWorld.playSound(
                        null,
                        arrow.getX(), arrow.getY(), arrow.getZ(),
                        SoundEvents.BLOCK_BAMBOO_BREAK,
                        SoundCategory.BLOCKS,
                        1.2F, 1.0F
                );

                // 2. 播放竹子方块粉碎粒子
                serverWorld.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BAMBOO.getDefaultState()),
                        arrow.getX(), arrow.getY(), arrow.getZ(),
                        15,
                        0.15, 0.15, 0.15,
                        0.05
                );
            }

            // 3. 销毁实体，防止被玩家拾取回收
            arrow.discard();
        }
    }
}
