package redmaple.obscurerevival.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.ObscureRevival;

public class ArcheryModificationBlueprintItem extends Item {

    private static final Formatting TITLE_FORMATTING = Formatting.GRAY;
    private static final Formatting DESCRIPTION_FORMATTING = Formatting.BLUE;

    private static final Text APPLIES_TO_TITLE_TEXT =
            Text.translatable("item.obscurerevival.archery_modification_blueprint.applies_to_title").formatted(TITLE_FORMATTING);
    private static final Text APPLIES_TO_TEXT =
            Text.translatable("item.obscurerevival.archery_modification_blueprint.applies_to").formatted(DESCRIPTION_FORMATTING);
    private static final Text INGREDIENTS_TITLE_TEXT =
            Text.translatable("item.obscurerevival.archery_modification_blueprint.ingredients_title").formatted(TITLE_FORMATTING);
    private static final Text INGREDIENTS_TEXT =
            Text.translatable("item.obscurerevival.archery_modification_blueprint.ingredients").formatted(DESCRIPTION_FORMATTING);

    private static final Text BASE_SLOT_DESCRIPTION_TEXT =
            Text.translatable("container.obscurerevival.fletching.base_slot_description");
    private static final Text ADDITIONS_SLOT_DESCRIPTION_TEXT =
            Text.translatable("container.obscurerevival.fletching.additions_slot_description");

    public static final Identifier EMPTY_SLOT_BLUEPRINT_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/archery_modification_blueprint");
    public static final Identifier EMPTY_SLOT_BOW_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/bow");
    public static final Identifier EMPTY_SLOT_CROSSBOW_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/crossbow");

    public static final Identifier EMPTY_SLOT_BAMBOO_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/bamboo");
    public static final Identifier EMPTY_SLOT_BREEZE_ROD_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/breeze_rod");

    public static final Identifier EMPTY_SLOT_COMPARATOR_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/comparator");
    public static final Identifier EMPTY_SLOT_DROPPER_TEXTURE =
            Identifier.of(ObscureRevival.MOD_ID, "container/slot/dropper");

    private final Text baseSlotDescriptionText;
    private final Text additionsSlotDescriptionText;
    private final List<Identifier> emptyBaseSlotTextures;

    public ArcheryModificationBlueprintItem(
            Text baseSlotDescriptionText,
            Text additionsSlotDescriptionText,
            List<Identifier> emptyBaseSlotTextures,
            Settings settings
    ) {
        super(settings);
        this.baseSlotDescriptionText = baseSlotDescriptionText;
        this.additionsSlotDescriptionText = additionsSlotDescriptionText;
        this.emptyBaseSlotTextures = emptyBaseSlotTextures;
    }

    public static ArcheryModificationBlueprintItem createArcheryModificationBlueprint(Settings settings) {
        return new ArcheryModificationBlueprintItem(
                BASE_SLOT_DESCRIPTION_TEXT,
                ADDITIONS_SLOT_DESCRIPTION_TEXT,
                List.of(EMPTY_SLOT_BOW_TEXTURE, EMPTY_SLOT_CROSSBOW_TEXTURE),
                settings
        );
    }

    @Override @SuppressWarnings("deprecation")
    public void appendTooltip(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        textConsumer.accept(ScreenTexts.EMPTY);
        textConsumer.accept(APPLIES_TO_TITLE_TEXT);
        textConsumer.accept(ScreenTexts.space().append(APPLIES_TO_TEXT));
        textConsumer.accept(INGREDIENTS_TITLE_TEXT);
        textConsumer.accept(ScreenTexts.space().append(INGREDIENTS_TEXT));
    }

    // 半透明剪影（Ghost Icons）唯一数据提供者接口。*/

    public Text getBaseSlotDescription() {
        return this.baseSlotDescriptionText;
    }

    public Text getAdditionsSlotDescription() {
        return this.additionsSlotDescriptionText;
    }

    public Identifier getEmptyBlueprintSlotTexture() {
        return EMPTY_SLOT_BLUEPRINT_TEXTURE;
    }

    public List<Identifier> getEmptyBaseSlotTextures() {
        return this.emptyBaseSlotTextures;
    }

    /**
     * 核心修正：严格的三段式递进逻辑。
     * 如果底模为空，返回空列表（不显示任何材料剪影）；
     * 如果底模是弓，轮播竹子和旋风棒；
     * 如果底模是弩，轮播投掷器和比较器。
     */
    public List<Identifier> getEmptyAdditionsSlotTextures(ItemStack baseStack) {
        if (baseStack.isEmpty()) {
            return List.of(); // 底模为空时，材料槽保持全空
        } else if (baseStack.isIn(ItemTags.BOW_ENCHANTABLE)) {
            return List.of(EMPTY_SLOT_BAMBOO_TEXTURE, EMPTY_SLOT_BREEZE_ROD_TEXTURE);
        } else if (baseStack.isIn(ItemTags.CROSSBOW_ENCHANTABLE)) {
            return List.of(EMPTY_SLOT_DROPPER_TEXTURE, EMPTY_SLOT_COMPARATOR_TEXTURE);
        }
        return List.of();
    }
}