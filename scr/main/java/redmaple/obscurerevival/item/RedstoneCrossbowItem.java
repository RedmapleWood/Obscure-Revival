package redmaple.obscurerevival.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RedstoneCrossbowItem extends CrossbowItem {

    public RedstoneCrossbowItem(Settings settings) {
        super(settings);
    }

    /**
     * 右键触发入口：
     * <li>1. 若弩已装填，触发单发重型发射，并施加 5 秒（100 ticks）长冷却；
     * <li>2. 若弩未装填，进入常规蓄力装填。
     */
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ChargedProjectilesComponent chargedComponent = stack.get(DataComponentTypes.CHARGED_PROJECTILES);

        if (chargedComponent != null && !chargedComponent.isEmpty()) {
            this.shootAll(world, user, hand, stack, getProjectileSpeed(chargedComponent), 1.0F, null);
            user.getItemCooldownManager().set(stack, 100);// 5 秒长冷却（100 ticks）
            return ActionResult.CONSUME;
        } else if (!user.getProjectileType(stack).isEmpty()) { user.setCurrentHand(hand); return ActionResult.CONSUME; }

        return ActionResult.FAIL;
    }

    /** 核心投掷物工厂方法。*/
    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        if (projectileStack.isOf(Items.FIREWORK_ROCKET)) { // 发射烟花火箭逻辑正常触发
            return new FireworkRocketEntity(world, projectileStack, shooter, shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ(), true);
        }

        ProjectileEntity projectileEntity = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);

        if (projectileEntity instanceof PersistentProjectileEntity arrow) {
            arrow.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
            arrow.setDamage(4.0); // 基础伤害伪力量附魔增伤公式：q = 0.5p + 0.5（p为附魔等级、q为增加的基础伤害）
        }

        return projectileEntity;
    }

    /** 原版 CrossbowItem.getSpeed 同构方法（消除 private 访问受限）。*/
    private static float getProjectileSpeed(ChargedProjectilesComponent stack) {
        return stack.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }
}