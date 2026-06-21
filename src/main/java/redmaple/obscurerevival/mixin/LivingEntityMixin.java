package redmaple.obscurerevival.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import redmaple.obscurerevival.refined_chain_armor.KeloidEnchantmentManager;
import redmaple.obscurerevival.refined_chain_armor.KeloidStateAccess;
import redmaple.obscurerevival.refined_chain_armor.SlashDefenceManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements KeloidStateAccess {

	// 层数由 CustomizedAttributes.KELOID_ARMOR 属性值唯一存储
	@Unique private int obscure_revival$keloidTimer = 0;
	@Unique private int obscure_revival$keloidFailCount = 0;

	@Override public int obscure_revival$getKeloidTimer() { return this.obscure_revival$keloidTimer; }
	@Override public void obscure_revival$setKeloidTimer(int timer) { this.obscure_revival$keloidTimer = timer; }
	@Override public int obscure_revival$getKeloidFailCount() { return this.obscure_revival$keloidFailCount; }
	@Override public void obscure_revival$setKeloidFailCount(int count) { this.obscure_revival$keloidFailCount = count; }

	/**
	 * 转发 tick 事件给 KeloidEnchantmentManager
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void onKeloidTick(CallbackInfo ci) {
		KeloidEnchantmentManager.handleTick((LivingEntity) (Object) this);
	}

	/**
	 * 拦截原版的 modifyAppliedDamage 方法，
	 * 在原版护甲值和保护附魔结算完毕后（RETURN 阶段），再执行剑/斧特化减伤
	 */
	@Inject(at = @At("RETURN"), method = "modifyAppliedDamage", cancellable = true)
	private void applyChainmailSlashingDefense(DamageSource source, float amount,
	                                           CallbackInfoReturnable<Float> cir) {
		float damageAfterVanilla = cir.getReturnValue();
		if (damageAfterVanilla > 0) {
			float finalDamage = SlashDefenceManager.applySlashingDefense(
					(LivingEntity) (Object) this,
					source,
					damageAfterVanilla
			);
			cir.setReturnValue(finalDamage);
		}
	}
}
