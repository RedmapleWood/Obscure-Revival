package redmaple.obscurerevival.registry;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseEffectsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import redmaple.obscurerevival.ObscureRevival;
import redmaple.obscurerevival.item.*;

public final class ORItems {

    public static final Item BAMBOO_BOW = register("bamboo_bow",
            settings -> new BambooBowItem(settings
                    .maxDamage(384)
                    .enchantable(1)
                    .repairable(Items.BAMBOO)
                    .component(DataComponentTypes.USE_EFFECTS, new UseEffectsComponent(false, true, 0.6F))));

    public static final Item BREEZE_BOW = register("breeze_bow",
            settings -> new BreezeBowItem(settings
                    .maxDamage(447)
                    .enchantable(1)
                    .repairable(Items.BREEZE_ROD)));

    public static final Item REDSTONE_CROSSBOW = register("redstone_crossbow",
            settings -> new RedstoneCrossbowItem(settings
                    .maxDamage(465)
                    .enchantable(1)
                    .repairable(Items.COMPARATOR)));

    public static final Item REPEATING_CROSSBOW = register("repeating_crossbow",
            settings -> new RepeatingCrossbowItem(settings
                    .maxDamage(465)
                    .enchantable(1)
                    .repairable(Items.DROPPER)));

    public static final Item ARCHERY_MODIFICATION_BLUEPRINT = register(
            "archery_modification_blueprint", ArcheryModificationBlueprintItem::createArcheryModificationBlueprint,
            new Item.Settings().rarity(Rarity.UNCOMMON));

    /** 辅助注册方法：1.21.2+ 原版规范要求将 RegistryKey<Item> 注入到 Item.Settings 中。*/
    private static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ObscureRevival.MOD_ID, path));
        Item item = factory.apply(settings.registryKey(registryKey));
        return Registry.register(Registries.ITEM, registryKey, item);
    }

    private static Item register(String path, Function<Item.Settings, Item> factory) { return register(path, factory, new Item.Settings()); }

    /** 将武器精准注入创造模式物品栏 （战斗栏 ItemGroups.COMBAT）。*/
    private static void registerItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.addAfter(Items.BOW, BAMBOO_BOW, BREEZE_BOW);
            entries.addAfter(Items.CROSSBOW, REPEATING_CROSSBOW, REDSTONE_CROSSBOW);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries ->
                entries.addAfter(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, ARCHERY_MODIFICATION_BLUEPRINT));
    }

    /** 注册自定义燃料（同构原版弓的燃烧时长：baseSmeltTime * 3 / 2 = 300 ticks）。*/
    private static void registerFuels() {
        FuelRegistryEvents.BUILD.register((builder, context) -> builder.add(BAMBOO_BOW, context.baseSmeltTime() * 3 / 2));
    }

    public static void register() { registerItemGroups(); registerFuels(); }

    private ORItems() {}
}