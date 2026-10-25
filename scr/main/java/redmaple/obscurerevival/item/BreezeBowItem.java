package redmaple.obscurerevival.item;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public class BreezeBowItem extends BowItem {

    public BreezeBowItem(Settings settings) { super(settings); }

    /** 风弓专属蓄力进度曲线计算（13 ticks 拉满）*/
    public static float getCustomPullProgress(int useTicks) {
        float progress = (float) useTicks / 13.0F;
        progress = (progress * progress + progress * 2.0F) / 3.0F;
        if (progress > 1.0F) { progress = 1.0F; }
        return progress;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity playerEntity)) { return false; }

        ItemStack projectileType = playerEntity.getProjectileType(stack);
        if (projectileType.isEmpty()) { return false; }

        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float pullProgress = getCustomPullProgress(useTicks);

        if ((double) pullProgress < 0.1) { return false; }

        List<ItemStack> list = load(stack, projectileType, playerEntity);
        if (world instanceof ServerWorld serverWorld) {
            if (!list.isEmpty()) {
                float speed = pullProgress * 3.0F * 1.15F;// 初速度内联赋予原版 1.15 倍初速
                this.shootAll(serverWorld, playerEntity, playerEntity.getActiveHand(), stack, list, speed, 1.0F, pullProgress == 1.0F, null);
            }
        }

        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F);
        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        return true;
    }
}