package redmaple.obscurerevival.modified_arrow.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import java.util.List;

/**
 * 一条“以原材料 A 为中心”的互斥声明。
 * <p>
 * 统一不互斥原则：A 与 incompatibleWith 中的每一项互斥；
 * 但 incompatibleWith 内部的各项之间【默认不互斥】——它们若真要互斥，须由它们各自的材料文件自行声明。
 * 互斥后果 = 输出槽为空，不做任何解释。
 */
public record MaterialConflict(Identifier center, List<Identifier> incompatibleWith) {
    public MaterialConflict { incompatibleWith = List.copyOf(incompatibleWith); }

    public static final Codec<MaterialConflict> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("center").forGetter(MaterialConflict::center),
            Identifier.CODEC.listOf().fieldOf("incompatible_with").forGetter(MaterialConflict::incompatibleWith)
    ).apply(instance, MaterialConflict::new));
}
