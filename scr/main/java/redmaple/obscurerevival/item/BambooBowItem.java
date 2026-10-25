package redmaple.obscurerevival.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import org.jetbrains.annotations.Nullable;

public class BambooBowItem extends BowItem {
    public BambooBowItem(Settings settings) { super(settings); }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        super.shoot(shooter, projectile, index, speed, 0.2F, yaw, target);
    }
}