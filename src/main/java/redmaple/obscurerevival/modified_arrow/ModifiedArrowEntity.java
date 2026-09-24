package redmaple.obscurerevival.modified_arrow;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import redmaple.obscurerevival.ObscureRevivalComponents;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;
import redmaple.obscurerevival.modified_arrow.effect.SpecialEffectBehavior;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModifiedArrowEntity extends PersistentProjectileEntity {

    private static final TrackedData<Long> EFFECT_FLAGS = DataTracker.registerData(ModifiedArrowEntity.class, TrackedDataHandlerRegistry.LONG);

    private List<ArrowSpecialEffect> activeEffects;
    private double trackedBaseDamage = 2.0;
    private boolean effectsInitialized;

    public ModifiedArrowEntity(EntityType<? extends ModifiedArrowEntity> type, World world) {
        super(type, world);
        this.ensureEffectsNotNull();
    }

    public ModifiedArrowEntity(World world, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom) {
        super(ModifiedArrowEntities.MODIFIED_ARROW, owner, world, stack, shotFrom);
        this.ensureEffectsNotNull();
        this.applyComponent(stack);
    }

    public ModifiedArrowEntity(World world, double x, double y, double z, ItemStack stack, @Nullable ItemStack weapon) {
        super(ModifiedArrowEntities.MODIFIED_ARROW, x, y, z, world, stack, weapon);
        this.ensureEffectsNotNull();
        this.applyComponent(stack);
    }

    private void ensureEffectsNotNull() {
        if (this.activeEffects == null) {
            this.activeEffects = List.of();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(EFFECT_FLAGS, 0L);
    }

    @Override
    protected void setStack(ItemStack stack) {
        super.setStack(stack);
        this.applyComponent(stack);
    }

    private void applyComponent(ItemStack stack) {
        ModifiedArrowComponent component = stack.get(ObscureRevivalComponents.ARROW_STATS);

        List<ArrowSpecialEffect> newEffects = component == null
                ? new ArrayList<>()
                : new ArrayList<>(component.specialEffects());

        newEffects.sort(Comparator.comparingInt(ArrowSpecialEffect::bitIndex));

        if (!this.effectsInitialized) {
            for (int i = 0; i < newEffects.size(); i++) {
                newEffects.get(i).behavior().onInit(this);
            }
            this.effectsInitialized = true;
        }

        this.activeEffects = List.copyOf(newEffects);

        long flags = 0L;
        for (int i = 0; i < newEffects.size(); i++) {
            flags |= newEffects.get(i).bitMask();
        }
        this.dataTracker.set(EFFECT_FLAGS, flags);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (EFFECT_FLAGS.equals(data) && this.getEntityWorld().isClient()) {
            this.rebuildActiveEffectsFromFlags();
        }
    }

    private void rebuildActiveEffectsFromFlags() {
        long flags = this.dataTracker.get(EFFECT_FLAGS);
        List<ArrowSpecialEffect> effects = new ArrayList<>();

        for (ArrowSpecialEffect effect : ArrowSpecialEffect.values()) {
            if ((flags & effect.bitMask()) != 0) {
                effects.add(effect);
            }
        }
        this.activeEffects = List.copyOf(effects);

        if (!this.effectsInitialized) {
            for (int i = 0; i < this.activeEffects.size(); i++) {
                this.activeEffects.get(i).behavior().onInit(this);
            }
            this.effectsInitialized = true;
        }
    }

    @Override
    public void setDamage(double damage) {
        super.setDamage(damage);
        this.trackedBaseDamage = damage;
    }

    /**
     * 只读地把当前特效链的伤害管道跑一遍（不写回 setDamage），
     * 供命中侧钩子查询"这支箭对某目标即将造成的真实伤害"（穿甲展开后进入护甲结算前的值）。
     */
    public double computeFinalDamageFor(LivingEntity target) {
        double damage = this.trackedBaseDamage;
        for (int i = 0; i < this.activeEffects.size(); i++) {
            damage = this.activeEffects.get(i).behavior().modifyDamage(damage, target);
        }
        return damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isInGround()) {
            // 1. 空气阻力解析：读取 getDrag()，若非原版默认 0.99F 则平滑应用
            if (!this.isTouchingWater()) {
                float drag = this.getDrag();
                if (drag != 0.99F) {
                    this.setVelocity(this.getVelocity().multiply(drag / 0.99F));
                }
            }

            // 2. 遍历触发各特异效果的 onTick 钩子
            for (int i = 0; i < this.activeEffects.size(); i++) {
                this.activeEffects.get(i).behavior().onTick(this);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        boolean handled = false;

        for (int i = 0; i < this.activeEffects.size(); i++) {
            if (this.activeEffects.get(i).behavior().onEntityHit(this, entityHitResult)) {
                handled = true;
            }
        }

        if (!handled) {
            if (entityHitResult.getEntity() instanceof LivingEntity target) {
                double originalDamage = this.trackedBaseDamage;
                // 与 computeFinalDamageFor 共用完整伤害管道（base → 全部特效 modifyDamage）
                double modifiedDamage = this.computeFinalDamageFor(target);

                this.setDamage(modifiedDamage);
                super.onEntityHit(entityHitResult);
                this.setDamage(originalDamage);
            } else {
                super.onEntityHit(entityHitResult);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        // 优先触发行为钩子（保留 isCritical 完整上下文）
        for (int i = 0; i < this.activeEffects.size(); i++) {
            this.activeEffects.get(i).behavior().onBlockHit(this, blockHitResult);
        }

        // 若箭矢未被销毁（如竹子满弓粉碎），继续走原版流程
        if (this.isAlive()) {
            super.onBlockHit(blockHitResult);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.activeEffects != null) {
            for (int i = 0; i < this.activeEffects.size(); i++) {
                this.activeEffects.get(i).behavior().onRemoved(this);
            }
        }
    }

    protected float getDrag() {
        for (int i = 0; i < this.activeEffects.size(); i++) {
            Float override = this.activeEffects.get(i).behavior().overrideDrag();
            if (override != null) {
                return override;
            }
        }
        return 0.99F;
    }

    @Override
    protected float getDragInWater() {
        for (int i = 0; i < this.activeEffects.size(); i++) {
            Float override = this.activeEffects.get(i).behavior().overrideDragInWater();
            if (override != null) {
                return override;
            }
        }
        return super.getDragInWater();
    }

    @Override
    protected double getGravity() {
        for (int i = 0; i < this.activeEffects.size(); i++) {
            Double override = this.activeEffects.get(i).behavior().overrideGravity();
            if (override != null) {
                return override;
            }
        }
        return super.getGravity();
    }

    @Override
    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        for (int i = 0; i < this.activeEffects.size(); i++) {
            Vec3d customVel = this.activeEffects.get(i).behavior().overrideVelocity(this, x, y, z, power, uncertainty);
            if (customVel != null) {
                this.setVelocity(customVel);
                this.velocityDirty = true;
                double d = customVel.horizontalLength();
                // noinspection SuspiciousNameCombination
                this.setYaw((float) (MathHelper.atan2(customVel.x, customVel.z) * (180.0F / (float) Math.PI)));
                this.setPitch((float) (MathHelper.atan2(customVel.y, d) * (180.0F / (float) Math.PI)));
                this.lastYaw = this.getYaw();
                this.lastPitch = this.getPitch();
                return;
            }
        }

        float finalPower = power;
        float finalUncertainty = uncertainty;

        for (int i = 0; i < this.activeEffects.size(); i++) {
            SpecialEffectBehavior b = this.activeEffects.get(i).behavior();
            finalPower = b.modifyLaunchPower(finalPower);
            finalUncertainty = b.modifyLaunchUncertainty(finalUncertainty);
        }

        super.setVelocity(x, y, z, finalPower, finalUncertainty);
    }

    @Override
    public boolean isInGround() {
        return super.isInGround();
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModifiedArrowItems.MODIFIED_ARROW);
    }
}
