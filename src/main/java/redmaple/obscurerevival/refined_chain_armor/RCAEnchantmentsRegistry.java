package redmaple.obscurerevival.refined_chain_armor;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RCAEnchantmentsRegistry {
    //因为是数据驱动，这里不需要像物品那样在主类显式调用Registry.register(),直接在此类声明常量即可
    public static final RegistryKey<Enchantment> KELOID = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("obscure_revival", "keloid"));
}
