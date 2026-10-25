package redmaple.obscurerevival;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.client.gui.hud.KeloidHudRenderer;
import redmaple.obscurerevival.client.gui.screen.ingame.FletchingScreen;
import redmaple.obscurerevival.registry.ORScreenHandlerTypes;

public class ObscureRevivalClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ObscureRevival.MOD_ID);

    @Override
    public void onInitializeClient() {
        KeloidHudRenderer.register();
        HandledScreens.register(ORScreenHandlerTypes.FLETCHING, FletchingScreen::new);
        LOGGER.info("\"Customized Client Rendering\" loaded!");
    }
}
