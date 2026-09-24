package redmaple.obscurerevival.modified_arrow;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import redmaple.obscurerevival.ObscureRevivalComponents;
import redmaple.obscurerevival.client.gui.tooltip.ArrowStatTooltipFormatter;
import redmaple.obscurerevival.modified_arrow.effect.ArmorPierceBehavior;

import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * 改装箭矢物品类。
 * <p>
 * 职责：继承原版 ArrowItem 作为弩发射及发射器调用入口；
 * 对黑曜石破甲箭额外承担"弩发射后的清算"：弩冷却 10 秒（200 刻）。
 * 真实箭路径（多重射击分身箭除外）仅清算一次。
 */
public class ModifiedArrowItem extends ArrowItem {
    private final WeakHashMap<LivingEntity, ShootTracker> shootTrackers = new WeakHashMap<>();
    public ModifiedArrowItem(Item.Settings settings) { super(settings); }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
        if (shotFrom != null && shotFrom.isOf(Items.CROSSBOW)) {
            ShootTracker tracker = this.shootTrackers.computeIfAbsent(shooter, k -> new ShootTracker());
            long currentTick = world.getTime();

            if (tracker.lastTick != currentTick) { tracker.lastTick = currentTick; tracker.count = 0; }
            tracker.count++;

            if (tracker.count > 1) {
                // 多重射击分身：委托原版箭，防刷物（创造模式专属拾取）
                ArrowItem vanillaArrowItem = (ArrowItem) Items.ARROW;
                PersistentProjectileEntity phantomArrow = vanillaArrowItem.createArrow(world, new ItemStack(Items.ARROW), shooter, shotFrom);
                phantomArrow.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                return phantomArrow;
            }
        }

        // ---- 黑曜石破甲箭 · 弩发射后清算（真实箭路径，同 Tick 仅一次）----
        if (shotFrom != null && shotFrom.isOf(Items.CROSSBOW) && shooter instanceof PlayerEntity player) {
            if (ArmorPierceBehavior.isPiercingArrow(stack)) {
                player.getItemCooldownManager().set(shotFrom, 200); // 弩冷却 10 秒
            }
        }

        return new ModifiedArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        ModifiedArrowEntity entity = new ModifiedArrowEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1), null);
        entity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        return entity;
    }

    private static class ShootTracker { long lastTick = -1; int count = 0; }

    @Override @SuppressWarnings("deprecation")// 未来一段时间内没有项目升级计划
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        ModifiedArrowComponent component = stack.get(ObscureRevivalComponents.ARROW_STATS);
        if (component != null) {
            for (Text line : ArrowStatTooltipFormatter.formatFinishedArrowTooltip(component)) {
                textConsumer.accept(line);
            }
        }
    }
}
