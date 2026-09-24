package redmaple.obscurerevival.modified_arrow;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 改装箭矢物品注册托管类。
 * <p>
 * 职责：集中管理模组自定义箭矢物品的静态注册，
 * 并向原版发射器 (DispenserBlock) 注册弹射物发射行为。
 */
public final class ModifiedArrowItems {

    private static final RegistryKey<Item> MODIFIED_ARROW_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of("obscure_revival", "modified_arrow"));

    public static final Item MODIFIED_ARROW = Registry.register(
            Registries.ITEM,
            MODIFIED_ARROW_KEY,
            new ModifiedArrowItem(new Item.Settings().registryKey(MODIFIED_ARROW_KEY))
    );

    private ModifiedArrowItems() {}

    /** 初始化此物品与注册发射器行为 */
    public static void init() {
        DispenserBlock.registerBehavior(MODIFIED_ARROW, new ProjectileDispenserBehavior(MODIFIED_ARROW));
    }
}
