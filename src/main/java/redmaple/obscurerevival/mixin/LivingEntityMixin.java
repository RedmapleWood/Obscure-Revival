package redmaple.obscurerevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowEntity;
import redmaple.obscurerevival.modified_arrow.effect.ArmorPierceBehavior;
import redmaple.obscurerevival.refined_chain_armor.KeloidEnchantmentManager;
import redmaple.obscurerevival.refined_chain_armor.KeloidStateAccess;
import redmaple.obscurerevival.refined_chain_armor.SlashDefenceManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements KeloidStateAccess {

	@Unique private int obscure_revival$keloidTimer = 0;
	@Unique private int obscure_revival$keloidFailCount = 0;

	@Override public int obscure_revival$getKeloidTimer() { return this.obscure_revival$keloidTimer; }
	@Override public void obscure_revival$setKeloidTimer(int timer) { this.obscure_revival$keloidTimer = timer; }
	@Override public int obscure_revival$getKeloidFailCount() { return this.obscure_revival$keloidFailCount; }
	@Override public void obscure_revival$setKeloidFailCount(int count) { this.obscure_revival$keloidFailCount = count; }

	/** 转发 tick 给战痕状态机 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void onKeloidTick(CallbackInfo ci) { KeloidEnchantmentManager.handleTick((LivingEntity) (Object) this); }

	/**
	 * 拦截原版护甲减伤计算出口。
	 * <p>伤害流程顺序：
	 * {@code applyArmorToDamage}（基础护甲运算）、{@code modifyAppliedDamage}（难度倍率修正）、{@code applyEnchantmentsToDamage}（保护类附魔运算）
	 * <p>若受击来源判定为破甲箭，则转入 {@link ArmorPierceBehavior#computePiercedArmorOutput} 进行折算。
	 */
	@WrapOperation(
			method = "applyArmorToDamage",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/entity/DamageUtil;getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F")
	)
	private float obscure_revival$pierceArmorForObsidianArrow(
			LivingEntity wearer, float amount, DamageSource source, float armor, float toughness,
			Operation<Float> original) {
		if (!(source.getSource() instanceof ModifiedArrowEntity arrow) || !ArmorPierceBehavior.isPiercingArrow(arrow.getItemStack())) {
			return original.call(wearer, amount, source, armor, toughness);
		}
		return ArmorPierceBehavior.computePiercedArmorOutput(amount, armor, toughness);
	}

	/** 原版护甲与保护附魔结算完毕后，再执行剑/斧特化减伤 */
	@Inject(at = @At("RETURN"), method = "modifyAppliedDamage", cancellable = true)
	private void applyChainmailSlashingDefense(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
		float damageAfterVanilla = cir.getReturnValue();
		if (damageAfterVanilla > 0) {
			float finalDamage = SlashDefenceManager.applySlashingDefense(
					(LivingEntity) (Object) this, source, damageAfterVanilla);
			cir.setReturnValue(finalDamage);
		}
	}
}
