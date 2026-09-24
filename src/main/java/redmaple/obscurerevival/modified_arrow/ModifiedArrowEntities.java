package redmaple.obscurerevival.modified_arrow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModifiedArrowEntities {

    public static final EntityType<ModifiedArrowEntity> MODIFIED_ARROW = register(
            "modified_arrow",
            EntityType.Builder.<ModifiedArrowEntity>create(ModifiedArrowEntity::new, SpawnGroup.MISC)
                    .dropsNothing()
                    .dimensions(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(20)
    );

    @SuppressWarnings("SameParameterValue")
    // 消除针对于“String path”形参的警告 → ModifiedArrowEntities 类里目前只有一种箭矢（modified_arrow），只需调用一次 register
    private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("obscure_revival", path));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    private ModifiedArrowEntities() {}

    public static void init() {}
}
