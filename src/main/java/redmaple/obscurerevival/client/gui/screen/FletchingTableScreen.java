package redmaple.obscurerevival.client.gui.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redmaple.obscurerevival.client.gui.tooltip.ArrowStatTooltipFormatter;
import redmaple.obscurerevival.fletching_table.screen.FletchingTableScreenHandler;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;
import redmaple.obscurerevival.modified_arrow.material.ArrowMaterialRegistry;

/**
 * 制箭台客户端 GUI 界面。
 * <p>职责：负责绘制制箭台的背景贴图、槽位引导提示、材料特异效果悬浮提示，当材料组合无效（如互斥）时的红色大叉错误提示。
 */
public class FletchingTableScreen extends HandledScreen<FletchingTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("obscure_revival", "textures/gui/container/fletching.png");

    private static final Identifier ERROR_TEXTURE = Identifier.of("obscure_revival", "container/fletching/error");

    private static final Text ADD_ARROW_TOOLTIP = Text.translatable("container.obscure_revival.fletching_table.add_arrow");
    private static final Text ADD_ARROWHEAD_TOOLTIP = Text.translatable("container.obscure_revival.fletching_table.add_arrowhead");
    private static final Text ADD_SHAFT_TOOLTIP = Text.translatable("container.obscure_revival.fletching_table.add_shaft");
    private static final Text ADD_FLETCH_TOOLTIP = Text.translatable("container.obscure_revival.fletching_table.add_fletch");

    public FletchingTableScreen(FletchingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.titleX = 42;
        this.titleY = 7;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // 1. 绘制主背景贴图
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0f, 0.0f,
                this.backgroundWidth, this.backgroundHeight,
                256, 256
        );

        // 2. 绘制无效配方（互斥/错误）的红色大叉
        if (this.shouldShowError()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ERROR_TEXTURE, x + 95, y + 32, 28, 21);
        }
    }

    /**
     * 判定是否应该显示红色大叉。
     * 逻辑：任意输入槽(0~3)有物品，且输出槽(4)为空，即视为无效配方。
     */
    private boolean shouldShowError() {
        boolean hasArrow = this.handler.getSlot(FletchingTableScreenHandler.ARROW_SLOT).hasStack();
        boolean hasMaterial = this.handler.getSlot(FletchingTableScreenHandler.ARROWHEAD_SLOT).hasStack() ||
                this.handler.getSlot(FletchingTableScreenHandler.SHAFT_SLOT).hasStack() ||
                this.handler.getSlot(FletchingTableScreenHandler.FLETCH_SLOT).hasStack();
        boolean isOutputEmpty = !this.handler.getSlot(FletchingTableScreenHandler.OUTPUT_SLOT).hasStack();

        return hasArrow && hasMaterial && isOutputEmpty;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        if (this.focusedSlot == null) {
            super.drawMouseoverTooltip(context, mouseX, mouseY);
            return;
        }

        ItemStack arrowStack = this.handler.getSlot(FletchingTableScreenHandler.ARROW_SLOT).getStack();
        ItemStack focusedStack = this.focusedSlot.getStack();
        int slotId = this.focusedSlot.id;

        if (arrowStack.isEmpty()) {
            if (slotId == FletchingTableScreenHandler.ARROW_SLOT && focusedStack.isEmpty()) {
                context.drawTooltip(this.textRenderer, List.of(ADD_ARROW_TOOLTIP), mouseX, mouseY);
                return;
            }
        } else if (focusedStack.isEmpty()) {
            Text guide = switch (slotId) {
                case FletchingTableScreenHandler.ARROWHEAD_SLOT -> ADD_ARROWHEAD_TOOLTIP;
                case FletchingTableScreenHandler.SHAFT_SLOT -> ADD_SHAFT_TOOLTIP;
                case FletchingTableScreenHandler.FLETCH_SLOT -> ADD_FLETCH_TOOLTIP;
                default -> null;
            };
            if (guide != null) {
                context.drawTooltip(this.textRenderer, List.of(guide), mouseX, mouseY);
                return;
            }
        } else if (slotId != FletchingTableScreenHandler.OUTPUT_SLOT && slotId != FletchingTableScreenHandler.ARROW_SLOT) {
            Optional<List<Text>> materialLines = buildMaterialTooltip(slotId, focusedStack.getItem());
            if (materialLines.isPresent()) {
                context.drawTooltip(this.textRenderer,
                        buildFullTooltip(focusedStack, materialLines.get()), mouseX, mouseY);
                return;
            }
        }

        super.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private Optional<List<Text>> buildMaterialTooltip(int slotId, Item item) {
        ArrowStatTooltipFormatter.MaterialSlot slotEnum;
        Map<Item, Set<ArrowSpecialEffect>> registry;

        if (slotId == FletchingTableScreenHandler.ARROWHEAD_SLOT) {
            slotEnum = ArrowStatTooltipFormatter.MaterialSlot.ARROWHEAD;
            registry = ArrowMaterialRegistry.ARROWHEAD_EFFECTS;
        } else if (slotId == FletchingTableScreenHandler.SHAFT_SLOT) {
            slotEnum = ArrowStatTooltipFormatter.MaterialSlot.SHAFT;
            registry = ArrowMaterialRegistry.SHAFT_EFFECTS;
        } else if (slotId == FletchingTableScreenHandler.FLETCH_SLOT) {
            slotEnum = ArrowStatTooltipFormatter.MaterialSlot.FLETCH;
            registry = ArrowMaterialRegistry.FLETCH_EFFECTS;
        } else {
            return Optional.empty();
        }

        Set<ArrowSpecialEffect> effects = registry.get(item);
        if (effects == null || effects.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ArrowStatTooltipFormatter.formatMaterialTooltip(slotEnum, effects));
    }

    private List<Text> buildFullTooltip(ItemStack stack, List<Text> extraLines) {
        List<Text> basicLines = stack.getTooltip(
                Item.TooltipContext.create(this.client.world), this.client.player, TooltipType.Default.BASIC);

        List<Text> lines = new ArrayList<>(basicLines);
        lines.addAll(extraLines);

        if (this.client.options.advancedItemTooltips) {
            List<Text> advancedLines = stack.getTooltip(
                    Item.TooltipContext.create(this.client.world), this.client.player, TooltipType.Default.ADVANCED);
            if (advancedLines.size() > basicLines.size()) {
                lines.addAll(advancedLines.subList(basicLines.size(), advancedLines.size()));
            }
        }
        return lines;
    }
}
