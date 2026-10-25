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

	@Unique private int obscurerevival$keloidTimer = 0;
	@Unique private int obscurerevival$keloidFailCount = 0;

	@Override public int obscurerevival$getKeloidTimer() { return this.obscurerevival$keloidTimer; }
	@Override public void obscurerevival$setKeloidTimer(int timer) { this.obscurerevival$keloidTimer = timer; }
	@Override public int obscurerevival$getKeloidFailCount() { return this.obscurerevival$keloidFailCount; }
	@Override public void obscurerevival$setKeloidFailCount(int count) { this.obscurerevival$keloidFailCount = count; }

	/** 转发 tick 给战痕状态机 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void onKeloidTick(CallbackInfo ci) { KeloidEnchantmentManager.handleTick((LivingEntity) (Object) this); }

	/** 原版护甲与保护附魔结算完毕后，再执行剑/斧特化减伤。*/
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
