package redmaple.obscurerevival.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;

public final class ORLootTables {

    /**
     * 注册战利品表注入监听：
     * <p>采用 Fabric Loot API v3 的独立池注入机制，完全避开不可控的权重分母竞争，保证原版战利品生成期望不受任何干扰。
     */
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) { return; }  // 确保仅拦截原版内置数据包，防止非原版资源包热重载时造成数据污染

            if (LootTables.ANCIENT_CITY_CHEST.equals(key)) {
                injectDedicatedPool(tableBuilder, ORItems.REDSTONE_CROSSBOW, 0.05F);
                injectDedicatedPool(tableBuilder, ORItems.ARCHERY_MODIFICATION_BLUEPRINT, 0.15F);
            } else if (LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE_CHEST.equals(key)) {
                injectDedicatedPool(tableBuilder, ORItems.REPEATING_CROSSBOW, 0.1F);
            } else if (LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST.equals(key)) {
                injectDedicatedPool(tableBuilder, ORItems.BREEZE_BOW, 0.15F);
            } else if (LootTables.JUNGLE_TEMPLE_CHEST.equals(key)) {
                injectDedicatedPool(tableBuilder, ORItems.BAMBOO_BOW, 0.2F);
            } else if (LootTables.STRONGHOLD_LIBRARY_CHEST.equals(key)) {
                injectDedicatedPool(tableBuilder, ORItems.ARCHERY_MODIFICATION_BLUEPRINT, 0.35F);
            }
        });
    }

    /**
     * 独立专属池安全注入：
     * <p>挂载单个物品条目与独立的 RandomChanceLootCondition，绕开原版池子的未知权重总和，确保数学概率绝对精准，同时 0 稀释原版掉落物。
     */
    private static void injectDedicatedPool(LootTable.Builder tableBuilder, ItemConvertible item, float chance) {
        LootPool.Builder pool = LootPool.builder().with(ItemEntry.builder(item)).conditionally(RandomChanceLootCondition.builder(chance));
        tableBuilder.pool(pool);
    }

    private ORLootTables() {}
}