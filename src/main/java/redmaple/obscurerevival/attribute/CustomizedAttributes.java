package redmaple.obscurerevival.attribute;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class CustomizedAttributes {

    /**
     * 战痕护甲值属性。
     * setTracked(true) 让属性系统自动将任何变化同步到客户端，
     * 死亡、重生、断线重连等场景均由原版属性同步机制自动处理，
     * 无需手动发包或客户端缓存。
     */
    public static final RegistryEntry<EntityAttribute> KELOID_ARMOR =
            Registry.registerReference(
                    Registries.ATTRIBUTE,
                    Identifier.of("obscure_revival", "keloid_armor"),
                    new ClampedEntityAttribute(
                            "attribute.name.obscure_revival.keloid_armor",
                            0.0, // 默认值
                            0.0, // 最小值
                            8.0  // 最大值（4层*每层2点）
                    ).setTracked(true)
            );

    private CustomizedAttributes() {}

    public static void register() {
        // 触发静态初始化，完成属性注册
    }
}
