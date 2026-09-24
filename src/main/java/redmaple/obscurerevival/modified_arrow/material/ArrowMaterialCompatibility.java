package redmaple.obscurerevival.modified_arrow.material;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 箭矢材料互斥校验器（数据驱动版）。
 * <p>
 * 分层校验：
 * <li>1. 内置硬互斥（Java 兜底，JSON 无法覆盖）——保证物理自洽性；
 * <li>2. 数据驱动互斥（/reload 可热重载）——由 ArrowMaterialRulesLoader 归一化注入。
 * 互斥命中即返回 false，合成引擎输出空槽；不向玩家解释原因（模仿原版沉默行为）。
 */
public final class ArrowMaterialCompatibility {

    /** 归一化后的数据驱动无向边集（运行时持有者） */
    private static final Set<MaterialPair> EXCLUSIONS = new HashSet<>();

    private ArrowMaterialCompatibility() {}

    /**
     * 由加载器调用：将中心式声明展开为无向边集；
     * MaterialPair 构造时规范化排序 + HashSet 天然去重，重复声明无害。
     */
    public static void reload(Collection<MaterialConflict> conflicts) {
        EXCLUSIONS.clear();
        for (MaterialConflict conflict : conflicts) {
            for (Identifier target : conflict.incompatibleWith()) {
                EXCLUSIONS.add(new MaterialPair(conflict.center(), target));
            }
        }
    }

    /**三材料两两配对查表。空槽不参与互斥*/
    public static boolean isCompatible(@Nullable Item arrowhead, @Nullable Item shaft, @Nullable Item fletch) {
        List<Identifier> present = new ArrayList<>(3);
        Identifier h = itemId(arrowhead);
        if (h != null) present.add(h);
        Identifier s = itemId(shaft);
        if (s != null) present.add(s);
        Identifier f = itemId(fletch);
        if (f != null) present.add(f);

        for (int i = 0; i < present.size(); i++) {
            for (int j = i + 1; j < present.size(); j++) {
                MaterialPair pair = new MaterialPair(present.get(i), present.get(j));
                if (EXCLUSIONS.contains(pair)) {
                    return false; // 命中任何一条（内置或数据）即拦截
                }
            }
        }
        return true;
    }

    private static Identifier itemId(@Nullable Item item) { return item == null ? null : Registries.ITEM.getId(item); }

    /**无向互斥边：构造时按 ID 排序，(A,B) 与 (B,A) 恒为同一对象*/
    public record MaterialPair(Identifier a, Identifier b) {
        public MaterialPair { if (a.toString().compareTo(b.toString()) > 0) { Identifier t = a; a = b; b = t; }}
    }
}
