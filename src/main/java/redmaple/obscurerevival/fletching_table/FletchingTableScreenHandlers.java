package redmaple.obscurerevival.fletching_table;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.fletching_table.screen.FletchingTableScreenHandler;

public class FletchingTableScreenHandlers {
    public static final ScreenHandlerType<FletchingTableScreenHandler> FLETCHING_TABLE =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of("obscure_revival", "fletching_table"),
                    new ScreenHandlerType<>(FletchingTableScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES)
            );

    public static void register() {
        // 这个方法本身什么都不用做，调用它只是为了触发上面静态字段的初始化
    }
}
