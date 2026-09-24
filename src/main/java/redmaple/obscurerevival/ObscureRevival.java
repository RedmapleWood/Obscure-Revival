package redmaple.obscurerevival;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.modified_arrow.material.ArrowMaterialRulesLoader;
import redmaple.obscurerevival.vanilla_registry.CustomizedEntityAttributes;
import redmaple.obscurerevival.fletching_table.FletchingTableInteractionHandler;
import redmaple.obscurerevival.fletching_table.FletchingTableScreenHandlers;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowEntities;
import redmaple.obscurerevival.modified_arrow.ModifiedArrowItems;
import redmaple.obscurerevival.refined_chain_armor.KeloidEnchantmentManager;
import redmaple.obscurerevival.vanilla_registry.Sounds;


public class ObscureRevival implements ModInitializer {
	public static final String MOD_ID = "obscure_revival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册自定义实体属性
		CustomizedEntityAttributes.register();
		/* 注册制箭台的右键交互逻辑与屏幕 UI 处理器 */
		FletchingTableScreenHandlers.register();
		FletchingTableInteractionHandler.register();
		// 注册Fabric伤害后触发 keloid 减伤事件（举盾挡住时不激发战痕应激，无视blocked参数）
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
				KeloidEnchantmentManager.handleDamage(entity, source)
		);
		// 将 KELOID_ARMOR 属性添加到玩家实体的默认属性容器，属性系统的 setTracked(true) 会自动处理客户端同步
		// noinspection ConstantConditions
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes().add(CustomizedEntityAttributes.KELOID_ARMOR)
		);
		/* 初始化魔改箭矢体系 */
		ModifiedArrowItems.init();
		ModifiedArrowEntities.init();
		// 初始化此 Mod 里所有的自定义 Data Component 种类
		ObscureRevivalComponents.init();
		// 注册所有原版自定义音效事件 （SoundEvent）
		Sounds.register();
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(
				ArrowMaterialRulesLoader.ID, new ArrowMaterialRulesLoader()
		);
		LOGGER.info("\"Obscure Revival\" loaded!");
	}
}