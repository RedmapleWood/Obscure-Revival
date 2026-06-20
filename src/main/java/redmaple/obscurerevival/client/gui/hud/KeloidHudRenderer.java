/*
Obscure Revival © 2026 by Redmaple Wood is licensed under CC BY-NC-SA 4.0.
To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
(Optional but appreciated: If you include this mod in a modpack, consider letting me know!)
*/

package redmaple.obscurerevival.client.gui.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import redmaple.obscurerevival.attribute.CustomizedAttributes;

public class KeloidHudRenderer implements HudElement {

    /** 战痕护甲栏的唯一 HUD 标识符 */
    public static final Identifier KELOID_ARMOR_BAR_ID =
            Identifier.of("obscure_revival", "keloid_armor_bar");

    /** 与 KeloidEnchantmentManager 共用同一个修饰符 ID */
    private static final Identifier KELOID_ARMOR_MODIFIER_ID =
            Identifier.of("obscure_revival", "keloid_armor_boost");

    // 原版护甲槽底图
    private static final Identifier ARMOR_EMPTY =
            Identifier.of("minecraft", "hud/armor_empty");

    // 战痕整格图标（战痕值每次以+2整数层增长，不存在半格情况）
    private static final Identifier KELOID_FULL =
            Identifier.of("obscure_revival", "hud/keloid_full");

    public static void register() {
        // 1. 拦截原版护甲栏：渲染前临时移除原版 ARMOR 属性上的战痕修饰符，
        //    让原版只渲染纯净护甲值，渲染完毕后立即还原
        HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR,
                originalElement -> (context, tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) {
                        originalElement.render(context, tickCounter);
                        return;
                    }
                    int keloidArmor = getKeloidArmorValue(client.player);
                    if (keloidArmor <= 0) {
                        originalElement.render(context, tickCounter);
                        return;
                    }
                    PlayerEntity player = client.player;
                    EntityAttributeInstance armorInstance =
                            player.getAttributeInstance(EntityAttributes.ARMOR);
                    if (armorInstance == null) {
                        originalElement.render(context, tickCounter);
                        return;
                    }
                    EntityAttributeModifier modifier =
                            armorInstance.getModifier(KELOID_ARMOR_MODIFIER_ID);
                    if (modifier == null) {
                        originalElement.render(context, tickCounter);
                        return;
                    }
                    armorInstance.removeModifier(KELOID_ARMOR_MODIFIER_ID);
                    originalElement.render(context, tickCounter);
                    armorInstance.addTemporaryModifier(modifier);
                });

        // 2. 将战痕护甲栏作为独立 HUD 组件插入原版护甲栏之后
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.ARMOR_BAR,
                KELOID_ARMOR_BAR_ID,
                new KeloidHudRenderer()
        );

        // 3. 高度占位：有战痕值时向上推开 10px，避免与原版护甲栏重叠
        HudStatusBarHeightRegistry.addLeft(KELOID_ARMOR_BAR_ID, player ->
                getKeloidArmorValue(player) > 0 ? 10 : 0
        );
    }

    @Override
    public void render(@NotNull DrawContext context, @NotNull RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int keloidArmor = getKeloidArmorValue(client.player);
        if (keloidArmor <= 0) return;

        int width  = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = width / 2 - 91;
        int y = height - HudStatusBarHeightRegistry.getHeight(KELOID_ARMOR_BAR_ID);

        // 战痕值每层+2，槽位数与层数一一对应
        int slots = keloidArmor / 2;

        for (int i = 0; i < slots; i++) {
            int iconX = x + i * 8;
            // 先画空底槽，再覆盖战痕整格图标
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_EMPTY, iconX, y, 9, 9);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, KELOID_FULL, iconX, y, 9, 9);
        }
    }

    /**
     * 从客户端本地的 KELOID_ARMOR 自定义属性读取当前战痕值。
     * 由 setTracked(true) 保证此值与服务端始终一致，
     * 死亡、重生、断线重连等场景均由原版属性同步机制自动处理。
     */
    private static int getKeloidArmorValue(PlayerEntity player) {
        EntityAttributeInstance keloidInstance =
                player.getAttributeInstance(CustomizedAttributes.KELOID_ARMOR);
        if (keloidInstance == null) return 0;
        return (int) keloidInstance.getValue();
    }
}