package redmaple.obscurerevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import redmaple.obscurerevival.modification.VillagerTradeManager;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    /**
     * 包装 fillRecipes 中对 fillRecipesFromPool 的调用：
     * <p>利用 MixinExtras 的 Operation 动态重算填充数量，绕过 Java protected 权限障碍。
     */
    @WrapOperation(
            method = "fillRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/VillagerEntity;fillRecipesFromPool(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/village/TradeOfferList;[Lnet/minecraft/village/TradeOffers$Factory;I)V"
            )
    )
    private void obscurerevival$wrapFletcherMasterTrades(VillagerEntity instance, ServerWorld world, TradeOfferList tradeOfferList, TradeOffers.Factory[] factories, int count, Operation<Void> original) {
        // 由业务管理器计算图纸注入并返回剩余需要抽取的原版数量
        int remainingCount = VillagerTradeManager.tryAddBlueprintTrade(instance, tradeOfferList, count);

        // 原汁原味将剩余配额交付原版执行（若图纸生成，此处为 1；若未生成，此处为 2）
        if (remainingCount > 0) { original.call(instance, world, tradeOfferList, factories, remainingCount); }
    }
}