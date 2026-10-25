package redmaple.obscurerevival.item;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;

public class RepeatingCrossbowItem extends CrossbowItem {

    public RepeatingCrossbowItem(Settings settings) { super(settings); }

    /**
     * 右键触发入口：
     * <li>1. 只要弹仓内有弹药（>0），执行单发点射并施加 15 ticks 冷却；
     * <li>2. 弹仓为空时，只要物品栏有弹药，进入长按拉弦蓄力状态。
     */
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ChargedProjectilesComponent chargedComponent = stack.get(DataComponentTypes.CHARGED_PROJECTILES);

        /* 1. 弹仓内只要有弹药，即可点发射击 */
        if (chargedComponent != null && !chargedComponent.isEmpty()) {
            this.shootSingleProjectile(world, user, hand, stack, chargedComponent);
            user.getItemCooldownManager().set(stack, 15);
            return ActionResult.CONSUME;
        }

        // 2. 弹仓为空，且物品栏有弹药，进入长按拉弦蓄力
        if (!user.getProjectileType(stack).isEmpty()) { user.setCurrentHand(hand); return ActionResult.CONSUME; }

        return ActionResult.FAIL;
    }

    /** 单发发射逻辑：弹出弹仓第 1 发发射，保留剩余弹药写回组件，彻底绕开原版 shootAll 清空弹仓的硬编码。*/
    private void shootSingleProjectile(World world, PlayerEntity shooter, Hand hand, ItemStack crossbow, ChargedProjectilesComponent chargedComponent) {
        if (!(world instanceof ServerWorld serverWorld)) { return; }
        List<ItemStack> projectiles = new ArrayList<>(chargedComponent.getProjectiles());
        if (projectiles.isEmpty()) { return; }

        ItemStack toShoot = projectiles.removeFirst();
        crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(projectiles));

        float speed = getProjectileSpeed(toShoot);
        this.shootAll(serverWorld, shooter, hand, crossbow, List.of(toShoot), speed, 1.0F, true, null);

        if (shooter instanceof ServerPlayerEntity serverPlayer) {
            Criteria.SHOT_CROSSBOW.trigger(serverPlayer, crossbow);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    /** 抄写原版 CrossbowItem.getSpeed 同构方法。*/
    private static float getProjectileSpeed(ItemStack projectile) { return projectile.isOf(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F; }

    /**
     * 装填状态机（装满或弹药耗尽后，进入静默空转，绝不调用 clearActiveItem，彻底杜绝走火）：
     * <li>阶段 1：基础拉弦（0.0 ~ 1.0）；
     * <li>阶段 2：拉满顶弦后，按间隔 8 ticks 连续向弹仓压弹，上限 5 发；
     */
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient()) { return; }
        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        int pullTime = getPullTime(stack, user);

        /* 阶段 1：拉弦基础音效 */
        float progress = (float) useTicks / (float) pullTime;
        float tickWindow = 1.0F / (float) pullTime;
        if (progress >= 0.2F && progress < 0.2F + tickWindow) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_START.value(), SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
        if (progress >= 0.5F && progress < 0.5F + tickWindow) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE.value(), SoundCategory.PLAYERS, 0.5F, 1.0F);
        }

        // 阶段 2：蓄力拉到顶后，连续步进装填 */
        if (useTicks >= pullTime) {
            ChargedProjectilesComponent currentComponent = stack.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
            List<ItemStack> currentList = currentComponent.getProjectiles();
            // 内联容量上限 5：若已满 5 发或物品栏无弹药，静默空转，保持使用状态防止走火
            if (currentList.size() >= 5 || user.getProjectileType(stack).isEmpty()) { return; }
            int extraTicks = useTicks - pullTime;
            // 内联 8 ticks 压弹间隔节奏
            if (extraTicks % 8 == 0) { this.tryLoadOneProjectile(user, stack, world, currentList); }
        }
    }

    /** 单发压弹执行器：处理多重射击附魔溢出裁剪、压弹音效及组件写回。*/
    private void tryLoadOneProjectile(LivingEntity user, ItemStack crossbow, World world, List<ItemStack> currentList) {
        if (currentList.size() >= 5) { return; } // 内联容量上限 5
        ItemStack projectileType = user.getProjectileType(crossbow);
        if (projectileType.isEmpty()) { return; }
        List<ItemStack> loaded = load(crossbow, projectileType, user);
        if (loaded.isEmpty()) { return; }
        List<ItemStack> updatedList = new ArrayList<>(currentList);

        /* 边界防护：处理多重射击等可能一次性抽出多发弹药导致的越界，严格裁剪至最多 5 发 */
        int availableSpace = 5 - updatedList.size();
        if (loaded.size() > availableSpace) {
            updatedList.addAll(loaded.subList(0, availableSpace));
        } else { updatedList.addAll(loaded); }

        crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(updatedList));

        // 清脆“咔哒”机械合膛音效
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END.value(), user.getSoundCategory(), 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.2F + 1.0F) + 0.2F);
    }

    /** 松开右键结算：同构原版生命周期，只要装入过弹药，松键后平滑结算完成。*/
    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        int pullTime = getPullTime(stack, user);
        return useTicks >= pullTime && isCharged(stack);
    }

    /** 覆写附魔接收规则：强行互斥“多重射击（Multishot）”。*/
    @Override
    public boolean canBeEnchantedWith(@NonNull ItemStack stack, RegistryEntry<Enchantment> enchantment, @NonNull EnchantingContext context) {
        if (enchantment.matchesKey(Enchantments.MULTISHOT)) { return false; }
        return super.canBeEnchantedWith(stack, enchantment, context);
    }
}