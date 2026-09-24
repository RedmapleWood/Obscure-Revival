package redmaple.obscurerevival;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.EntityRendererFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.client.gui.hud.KeloidHudRenderer;
import redmaple.obscurerevival.client.gui.screen.FletchingTableScreen;
import redmaple.obscurerevival.client.render.entity.ModifiedArrowEntityRenderer;
import redmaple.obscurerevival.fletching_table.FletchingTableScreenHandlers;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowEntities;

public class ObscureRevivalClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ObscureRevival.MOD_ID);

    @Override
    public void onInitializeClient() {
        // 注册战痕（Keloid）盔甲专属的自定义状态栏/UI覆盖层渲染
        KeloidHudRenderer.register();
        // 将服务端制箭台的 ScreenHandler （逻辑） 与客户端的 Screen （视觉界面） 进行配对映射
        HandledScreens.register(FletchingTableScreenHandlers.FLETCHING_TABLE, FletchingTableScreen::new);
        // 绑定改造箭矢实体的客户端 3D 渲染器
        EntityRendererFactories.register(ModifiedArrowEntities.MODIFIED_ARROW, ModifiedArrowEntityRenderer::new);
        LOGGER.info("\"Customized Client Rendering\" loaded!");
    }
}
