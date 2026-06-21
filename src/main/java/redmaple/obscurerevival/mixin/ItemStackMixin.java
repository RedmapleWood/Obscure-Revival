package redmaple.obscurerevival.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import redmaple.obscurerevival.refined_chain_armor.RCAEnchantmentsRegistry;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    /**
     *修改damage(int amount,LivingEntity user,EquipmentSlot slot)的第一个int参数
     */
    @ModifyVariable(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int applyKeloidDurabilityPenalty(int amount) {
        //1.通过堆栈自身获取对象
        ItemStack stack = (ItemStack) (Object) this;

        //2.读取附魔组件
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        if (enchantments.isEmpty()) { return amount; }
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            if (entry.matchesKey(RCAEnchantmentsRegistry.KELOID)) {
                int newAmount = amount * 2;//将惩罚翻倍
                return Math.max(0, newAmount);//防止返回负数或超过整型范围，按实情裁剪
            }
        }
        return amount;
    }
}
