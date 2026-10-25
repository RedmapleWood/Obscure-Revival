package redmaple.obscurerevival.client.gui.screen.ingame;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.ObscureRevival;
import redmaple.obscurerevival.item.ArcheryModificationBlueprintItem;
import redmaple.obscurerevival.registry.ORItems;
import redmaple.obscurerevival.screen.FletchingScreenHandler;

@Environment(EnvType.CLIENT)
public class FletchingScreen extends ForgingScreen<FletchingScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "textures/gui/container/fletching.png");

    private static final Identifier ERROR_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/fletching/error");

    private static final Text ERROR_TOOLTIP =
            Text.translatable("container.upgrade.error_tooltip");
    private static final Text MISSING_BLUEPRINT_TOOLTIP =
            Text.translatable("container.obscurerevival.fletching.missing_template_tooltip");

    private static final int ERROR_ICON_X = 92;
    private static final int ERROR_ICON_Y = 45;
    private static final int ERROR_ICON_WIDTH = 28;
    private static final int ERROR_ICON_HEIGHT = 21;

    private final CyclingSlotIcon blueprintSlotIcon = new CyclingSlotIcon(FletchingScreenHandler.BLUEPRINT_ID);
    private final CyclingSlotIcon baseSlotIcon = new CyclingSlotIcon(FletchingScreenHandler.BASE_ID);
    private final CyclingSlotIcon additionsSlotIcon = new CyclingSlotIcon(FletchingScreenHandler.ADDITION_ID);

    public FletchingScreen(FletchingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, TEXTURE);
        this.titleX = 52;
        this.titleY = 15;
    }

    private ArcheryModificationBlueprintItem getBlueprintDataProvider() {
        ItemStack blueprintStack = this.handler.getSlot(FletchingScreenHandler.BLUEPRINT_ID).getStack();
        if (blueprintStack.getItem() instanceof ArcheryModificationBlueprintItem blueprint) {
            return blueprint;
        }
        return (ArcheryModificationBlueprintItem) ORItems.ARCHERY_MODIFICATION_BLUEPRINT;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();

        ArcheryModificationBlueprintItem blueprint = this.getBlueprintDataProvider();

        ItemStack blueprintStack = this.handler.getSlot(FletchingScreenHandler.BLUEPRINT_ID).getStack();
        ItemStack baseStack = this.handler.getSlot(FletchingScreenHandler.BASE_ID).getStack();

        // 1. 图纸槽：永久显示图纸剪影
        this.blueprintSlotIcon.updateTexture(List.of(blueprint.getEmptyBlueprintSlotTexture()));

        // 2. 底模槽：放入图纸后才开始轮播弓/弩剪影
        if (blueprintStack.isEmpty()) {
            this.baseSlotIcon.updateTexture(List.of());
        } else {
            this.baseSlotIcon.updateTexture(blueprint.getEmptyBaseSlotTextures());
        }

        // 3. 材料槽：放入图纸且放入底模后才轮播
        if (blueprintStack.isEmpty() || baseStack.isEmpty()) {
            this.additionsSlotIcon.updateTexture(List.of());
        } else {
            this.additionsSlotIcon.updateTexture(blueprint.getEmptyAdditionsSlotTextures(baseStack));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        super.drawBackground(context, deltaTicks, mouseX, mouseY);
        this.blueprintSlotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
        this.baseSlotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
        this.additionsSlotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {
        if (this.hasInvalidRecipe()) {
            context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED,
                    ERROR_TEXTURE,
                    x + ERROR_ICON_X,
                    y + ERROR_ICON_Y,
                    ERROR_ICON_WIDTH,
                    ERROR_ICON_HEIGHT
            );
        }
    }

    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY) {
        Optional<Text> optionalTooltip = Optional.empty();

        if (this.hasInvalidRecipe() && this.isPointWithinBounds(
                ERROR_ICON_X, ERROR_ICON_Y, ERROR_ICON_WIDTH, ERROR_ICON_HEIGHT, mouseX, mouseY)) {
            optionalTooltip = Optional.of(ERROR_TOOLTIP);
        }

        if (this.focusedSlot != null) {
            ItemStack blueprintStack = this.handler.getSlot(FletchingScreenHandler.BLUEPRINT_ID).getStack();
            ItemStack hoveredSlotStack = this.focusedSlot.getStack();

            if (blueprintStack.isEmpty()) {
                if (this.focusedSlot.id == FletchingScreenHandler.BLUEPRINT_ID) {
                    optionalTooltip = Optional.of(MISSING_BLUEPRINT_TOOLTIP);
                }
            } else if (blueprintStack.getItem() instanceof ArcheryModificationBlueprintItem blueprint) {
                if (hoveredSlotStack.isEmpty()) {
                    if (this.focusedSlot.id == FletchingScreenHandler.BASE_ID) {
                        optionalTooltip = Optional.of(blueprint.getBaseSlotDescription());
                    } else if (this.focusedSlot.id == FletchingScreenHandler.ADDITION_ID) {
                        optionalTooltip = Optional.of(blueprint.getAdditionsSlotDescription());
                    }
                }
            }
        }

        optionalTooltip.ifPresent(text ->
                context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY)
        );
    }

    private boolean hasInvalidRecipe() {
        return this.handler.hasInvalidRecipe();
    }
}