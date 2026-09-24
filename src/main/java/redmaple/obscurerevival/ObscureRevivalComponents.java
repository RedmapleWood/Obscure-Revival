package redmaple.obscurerevival;

import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowComponent;

public final class ObscureRevivalComponents {
    public static final ComponentType<ModifiedArrowComponent> ARROW_STATS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("obscure_revival", "arrow_stats"),
            ComponentType.<ModifiedArrowComponent>builder()
                    .codec(ModifiedArrowComponent.CODEC)
                    .packetCodec(PacketCodecs.codec(ModifiedArrowComponent.CODEC))
                    .build()
    );

    private ObscureRevivalComponents() {}

    public static void init() {}
}
