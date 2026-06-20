/*
Obscure Revival © 2026 by Redmaple Wood is licensed under CC BY-NC-SA 4.0.
To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
(Optional but appreciated: If you include this mod in a modpack, consider letting me know!)
*/

package redmaple.obscurerevival;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.client.gui.hud.KeloidHudRenderer;
import static redmaple.obscurerevival.ObscureRevival.MOD_ID;

public class ObscureRevivalClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        KeloidHudRenderer.register();
        LOGGER.info("\"Customized Client Rendering\" loaded!");
    }
}
