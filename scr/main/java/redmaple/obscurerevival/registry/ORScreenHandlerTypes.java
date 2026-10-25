package redmaple.obscurerevival.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.ObscureRevival;
import redmaple.obscurerevival.screen.FletchingScreenHandler;

public final class ORScreenHandlerTypes {

    public static final ScreenHandlerType<FletchingScreenHandler> FLETCHING =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(ObscureRevival.MOD_ID, "fletching"),
                    new ScreenHandlerType<>(FletchingScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
            );

    public static void register() {
        ObscureRevival.LOGGER.info("\"Screen Handler Types\" registered successfully!");
    }

    private ORScreenHandlerTypes() {}
}