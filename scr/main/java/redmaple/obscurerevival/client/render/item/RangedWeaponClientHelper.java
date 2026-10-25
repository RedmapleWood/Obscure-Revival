package redmaple.obscurerevival.client.render.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import redmaple.obscurerevival.item.BreezeBowItem;

@Environment(EnvType.CLIENT)
public final class RangedWeaponClientHelper {

    /** 判断物品是否为弓（原版弓、Mod 继承类或具备弓附魔特性的武器）。*/
    public static boolean isBow(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BowItem || stack.isIn(ItemTags.BOW_ENCHANTABLE);
    }

    /** 判断物品是否为弩（原版弩、Mod 继承类或具备弩附魔特性的武器）。*/
    public static boolean isCrossbow(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof CrossbowItem || stack.isIn(ItemTags.CROSSBOW_ENCHANTABLE);
    }

    /** 获取弓拉满所需的总刻数（普通弓 20 ticks，风弓 13 ticks）。*/
    public static float getBowPullTicks(ItemStack stack) {
        if (stack.getItem() instanceof BreezeBowItem) { return 13.0F; }
        return 20.0F;
    }
    private RangedWeaponClientHelper() {}
}