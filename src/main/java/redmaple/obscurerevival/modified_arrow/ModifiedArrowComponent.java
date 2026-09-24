package redmaple.obscurerevival.modified_arrow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import redmaple.obscurerevival.modified_arrow.effect.ArrowSpecialEffect;

import java.util.List;
import java.util.Set;

/**改装箭矢数据组件 Record*/
public record ModifiedArrowComponent(Set<ArrowSpecialEffect> specialEffects) {

    // 紧凑构造函数：强制深拷贝为不可变集合，防御外部污染
    public ModifiedArrowComponent { specialEffects = specialEffects == null ? Set.of() : Set.copyOf(specialEffects); }

    public static final Codec<ModifiedArrowComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ArrowSpecialEffect.CODEC.listOf()
                    .xmap(Set::copyOf, List::copyOf)
                    .fieldOf("special_effects")
                    .forGetter(ModifiedArrowComponent::specialEffects)
    ).apply(instance, ModifiedArrowComponent::new));
}
