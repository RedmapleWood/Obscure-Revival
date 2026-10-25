package redmaple.obscurerevival;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.registry.ORRecipeTypes;
import redmaple.obscurerevival.registry.*;
import redmaple.obscurerevival.refined_chain_armor.KeloidEnchantmentManager;
import redmaple.obscurerevival.screen.FletchingTableInteractionHandler;

public class ObscureRevival implements ModInitializer {
	public static final String MOD_ID = "obscurerevival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册Fabric伤害后触发 keloid 减伤事件（举盾挡住时不激发战痕应激，无视blocked参数）
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
				KeloidEnchantmentManager.handleDamage(entity, source)
		);
		// 将 KELOID_ARMOR 属性添加到玩家实体的默认属性容器，属性系统的 setTracked(true) 会自动处理客户端同步
		// noinspection ConstantConditions
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes().add(OREntityAttributes.KELOID_ARMOR)
		);
		FletchingTableInteractionHandler.register();
		OREntityAttributes.register();
		ORItems.register();
		ORScreenHandlerTypes.register();
		ORSounds.register();
		ORLootTables.register();
		ORRecipeTypes.register();
		LOGGER.info("\"Obscure Revival\" loaded!");
	}
}