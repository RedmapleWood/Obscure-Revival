package redmaple.obscurerevival.vanilla_registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * 原版音效注册类。
 * <p>
 * 职责：作为模组所有自定义原版音效事件 （SoundEvent） 的集中注册中心，
 * 将 Identifier 字符串与其对应的 SoundEvent 实例绑定并统一托管至原版音效注册表中。
 */
public final class Sounds {

    /*制箭台取出成品音效*/
    public static final Identifier UI_FLETCHING_TABLE_TAKE_RESULT_ID = Identifier.of("obscure_revival", "ui.fletching_table.take_result");
    public static final SoundEvent UI_FLETCHING_TABLE_TAKE_RESULT = SoundEvent.of(UI_FLETCHING_TABLE_TAKE_RESULT_ID);

    private Sounds() {}

    /** 初始化并注册模组所有的自定义音效事件 */
    public static void register() {
        Registry.register(Registries.SOUND_EVENT, UI_FLETCHING_TABLE_TAKE_RESULT_ID, UI_FLETCHING_TABLE_TAKE_RESULT);
    }

    // 以上均为显式流程注册，确保音效加载时机可控；当前注册表规模有限，未来若新增条目超多，再考虑模仿SoundEvents进行重构
}
