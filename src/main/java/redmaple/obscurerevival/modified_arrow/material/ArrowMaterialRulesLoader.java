package redmaple.obscurerevival.modified_arrow.material;

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import redmaple.obscurerevival.ObscureRevival;
import java.util.Map;

/**
 * 互斥规则数据加载器。
 * <p>
 * 职责：读取 data/obscure_revival/mutual_exclusion/*.json，
 * 用 MaterialConflict.CODEC 异步解码，归一化后写入内存持有者。
 * 输入 /reload 命令会自动重跑。
 */
public class ArrowMaterialRulesLoader extends JsonDataLoader<MaterialConflict> {

    public static final Identifier ID = Identifier.of("obscure_revival", "mutual_exclusion");

    public ArrowMaterialRulesLoader() { super(MaterialConflict.CODEC, ResourceFinder.json("mutual_exclusion")); }

    @Override
    protected void apply(Map<Identifier, MaterialConflict> prepared, ResourceManager manager, Profiler profiler) {
        ArrowMaterialCompatibility.reload(prepared.values());
        ObscureRevival.LOGGER.info("Loaded {} arrow material mutual exclusion rules.", prepared.size());
    }
}
