package redmaple.obscurerevival.client.gui.tooltip;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 箭矢属性悬浮提示格式化工具。
 * <p>
 * 职责：负责将特异效果集合转换为玩家可读的本地化文本列表。
 */
public final class ArrowStatTooltipFormatter {

    private ArrowStatTooltipFormatter() {}

    public enum MaterialSlot {
        ARROWHEAD("tooltip.obscure_revival.fletching_table.slot_name.arrowhead"),
        SHAFT("tooltip.obscure_revival.fletching_table.slot_name.shaft"),
        FLETCH("tooltip.obscure_revival.fletching_table.slot_name.fletch");

        final String nameKey;

        MaterialSlot(String nameKey) { this.nameKey = nameKey; }
    }

    /** 格式化原材料槽位的悬浮提示（仅显示特异效果） */
    public static List<Text> formatMaterialTooltip(MaterialSlot slot, Set<ArrowSpecialEffect> effects) {
        List<Text> lines = new ArrayList<>();
        Text slotName = Text.translatable(slot.nameKey);

        lines.add(Text.translatable("tooltip.obscure_revival.fletching_table.after", slotName).formatted(Formatting.GRAY));

        for (ArrowSpecialEffect effect : effects) {
            lines.add(Text.translatable("tooltip.obscure_revival.arrow_special." + effect.name().toLowerCase())
                    .formatted(Formatting.GREEN));
        }
        return lines;
    }

    /** 格式化成品箭矢的悬浮提示（直接读取物品保存的组件效果） */
    public static List<Text> formatFinishedArrowTooltip(ModifiedArrowComponent component) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.translatable("tooltip.obscure_revival.fletching_table.fired_after").formatted(Formatting.GRAY));

        for (ArrowSpecialEffect effect : component.specialEffects()) {
            lines.add(Text.translatable("tooltip.obscure_revival.arrow_special." + effect.name().toLowerCase())
                    .formatted(Formatting.GREEN));
        }
        return lines;
    }
}
