package redmaple.obscurerevival.modification;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import redmaple.obscurerevival.registry.ORItems;

public final class VillagerTradeManager {

    /**
     * 判定并尝试注入大师级制箭师图纸交易条目。
     *
     * @param villager       村民实例
     * @param tradeOfferList 当前生成的交易清单
     * @param defaultCount   原版或其它模组默认需要抽取的条目数量
     * @return 扣减图纸占位后，仍需交由原版工厂池补齐抽取的剩余数量（严格保证 >= 0）
     */
    public static int tryAddBlueprintTrade(VillagerEntity villager, TradeOfferList tradeOfferList, int defaultCount) {
        VillagerData data = villager.getVillagerData();

        /* 仅处理 5 级（大师级）制箭师 */
        boolean isFletcher = data.profession().matchesKey(VillagerProfession.FLETCHER);
        if (!isFletcher || data.level() != 5) { return defaultCount; }

        Random random = villager.getRandom();


        if (random.nextFloat() < 0.60F) { // 60% 出现概率判定
            int emeraldPrice = random.nextBetween(45, 64);

            TradeOffer blueprintOffer = new TradeOffer(
                    new TradedItem(Items.EMERALD, emeraldPrice),
                    new ItemStack(ORItems.ARCHERY_MODIFICATION_BLUEPRINT),
                    1, // 失效前可交易次数
                    30,        // 村民获得的经验值
                    0.05F      // 价格变动乘数
            );

            // 占位 1：加入射艺改装图纸
            tradeOfferList.add(blueprintOffer);

            // 边界防御：即使原版或其它模组将每次升级生成的交易数修改为 1 或 0，严格做 (defaultCount - 1 > 0) 校验，彻底杜绝传入负数
            return Math.max(defaultCount - 1, 0);
        }

        // 40% 几率未中：名额保持原样
        return defaultCount;
    }

    private VillagerTradeManager() {}
}