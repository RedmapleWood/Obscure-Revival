package redmaple.obscurerevival.fletching_table;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import redmaple.obscurerevival.ObscureRevivalComponents;
import redmaple.obscurerevival.fletching_table.screen.FletchingTableScreenHandler;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowItems;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowComponent;
import redmaple.obscurerevival.modified_arrow.material.ArrowMaterialCompatibility;
import redmaple.obscurerevival.modified_arrow.material.ArrowMaterialRegistry;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * 制箭台物品合成引擎。
 * <p>
 * 职责：负责材料识别、ID匹配、黑名单过滤，并将各槽位材料的特异效果（ArrowSpecialEffect）
 * 汇总聚合，最终生成带有 ModifiedArrowComponent 的预览物品。
 */
public final class FletchingCraftingEngine {

    private FletchingCraftingEngine() {
    }

    /** 评估输入背包并执行具体合成逻辑 */
    public static Optional<ItemStack> craft(Inventory input) {
        ItemStack arrowStack = input.getStack(FletchingTableScreenHandler.ARROW_SLOT);
        ItemStack headStack = input.getStack(FletchingTableScreenHandler.ARROWHEAD_SLOT);
        ItemStack shaftStack = input.getStack(FletchingTableScreenHandler.SHAFT_SLOT);
        ItemStack fletchStack = input.getStack(FletchingTableScreenHandler.FLETCH_SLOT);

        // 1. 基础物品 ID 校验：只要 ID 是 minecraft:arrow 即可
        if (arrowStack.isEmpty() || !arrowStack.isOf(Items.ARROW)) {
            return Optional.empty();
        }

        Item headItem = headStack.isEmpty() ? null : headStack.getItem();
        Item shaftItem = shaftStack.isEmpty() ? null : shaftStack.getItem();
        Item fletchItem = fletchStack.isEmpty() ? null : fletchStack.getItem();

        // 2. 材料充要性校验：至少放入了一种有效材料
        boolean hasAnyMaterial = headItem != null || shaftItem != null || fletchItem != null;
        if (!hasAnyMaterial) {
            return Optional.empty();
        }

        // 3. 黑名单与兼容性校验 (基于规则链)
        if (!ArrowMaterialCompatibility.isCompatible(headItem, shaftItem, fletchItem)) {
            return Optional.empty();
        }

        // 4. 汇总所有特异效果
        Set<ArrowSpecialEffect> combinedEffects = EnumSet.noneOf(ArrowSpecialEffect.class);
        combinedEffects.addAll(ArrowMaterialRegistry.getEffects(ArrowMaterialRegistry.ARROWHEAD_EFFECTS, headStack));
        combinedEffects.addAll(ArrowMaterialRegistry.getEffects(ArrowMaterialRegistry.SHAFT_EFFECTS, shaftStack));
        combinedEffects.addAll(ArrowMaterialRegistry.getEffects(ArrowMaterialRegistry.FLETCH_EFFECTS, fletchStack));

        // 5. 构建成品并挂载组件
        ItemStack result = new ItemStack(ModifiedArrowItems.MODIFIED_ARROW, 1);
        result.set(ObscureRevivalComponents.ARROW_STATS, new ModifiedArrowComponent(combinedEffects));

        // 6. 铁砧自定义名称 (Custom Name) 继承
        if (arrowStack.contains(DataComponentTypes.CUSTOM_NAME)) {
            result.set(DataComponentTypes.CUSTOM_NAME, arrowStack.get(DataComponentTypes.CUSTOM_NAME));
        }

        return Optional.of(result);
    }
}
