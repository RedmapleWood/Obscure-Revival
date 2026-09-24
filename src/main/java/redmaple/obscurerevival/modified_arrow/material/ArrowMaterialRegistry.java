package redmaple.obscurerevival.modified_arrow.material;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;
import java.util.Map;
import java.util.Set;

/**
 * 箭矢材料特异效果注册表。
 * <p>职责：作为单源真理，定义每种原材料在被锻造为箭矢时，能够提供哪些特异效果。
 */
public class ArrowMaterialRegistry {

    public static final Map<Item, Set<ArrowSpecialEffect>> ARROWHEAD_EFFECTS = Map.of(
            Items.IRON_INGOT,       Set.of(ArrowSpecialEffect.DAMAGE_INCREASE),//完成（近距离暴击）
            Items.OBSIDIAN,         Set.of(ArrowSpecialEffect.ARMOR_PIERCE),//完成
            Items.PRISMARINE_SHARD, Set.of(ArrowSpecialEffect.UNDERWATER_NO_DRAG)//完成
    );

    public static final Map<Item, Set<ArrowSpecialEffect>> SHAFT_EFFECTS = Map.of(
            Items.BREEZE_ROD, Set.of(ArrowSpecialEffect.WIND_PUSH),
            Items.BAMBOO,     Set.of(ArrowSpecialEffect.DRAG_REDUCE)//完成
    );

    public static final Map<Item, Set<ArrowSpecialEffect>> FLETCH_EFFECTS = Map.of(
            Items.ENDER_EYE,        Set.of(ArrowSpecialEffect.ACCURACY_BOOST),//完成，但有箭追踪思路，以后再说
            Items.PHANTOM_MEMBRANE, Set.of(ArrowSpecialEffect.LIGHT)//完成
    );

    private ArrowMaterialRegistry() {}

    /**
     * 安全获取指定槽位物品的特异效果集合。
     * @param registry 对应的材料注册表（如 ARROWHEAD_EFFECTS）
     * @param stack    槽位中的物品堆
     * @return 物品附带的特异效果集合（若为空或未注册则返回空集合）
     */
    public static Set<ArrowSpecialEffect> getEffects(Map<Item, Set<ArrowSpecialEffect>> registry, ItemStack stack) {
        if (stack.isEmpty()) { return Set.of(); }
        return registry.getOrDefault(stack.getItem(), Set.of());
    }
}
