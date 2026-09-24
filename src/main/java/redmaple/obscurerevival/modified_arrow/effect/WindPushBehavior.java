package redmaple.obscurerevival.modified_arrow.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class WindPushBehavior implements SpecialEffectBehavior {

    private static final double PUSH_RADIUS = 3.0;
    private static final double PUSH_STRENGTH = 0.8;

    @Override
    public void onTick(PersistentProjectileEntity arrow) {
        if (arrow.getEntityWorld().isClient()) {
            return;
        }

        Box area = arrow.getBoundingBox().expand(PUSH_RADIUS);
        for (Entity entity : arrow.getEntityWorld().getOtherEntities(arrow, area, e -> e.isAlive() && !e.isSpectator())) {
            Vec3d away = entity.getEntityPos().subtract(arrow.getEntityPos()).normalize();
            entity.addVelocity(away.x * PUSH_STRENGTH, away.y * PUSH_STRENGTH * 0.3, away.z * PUSH_STRENGTH);
            entity.velocityDirty = true;
        }
    }
}
