/*
Obscure Revival © 2026 by Redmaple Wood is licensed under CC BY-NC-SA 4.0.
To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/
(Optional but appreciated: If you include this mod in a modpack, consider letting me know!)
*/

package redmaple.obscurerevival;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redmaple.obscurerevival.attribute.CustomizedAttributes;
import redmaple.obscurerevival.refined_chain_armor.KeloidEnchantmentManager;

public class ObscureRevival implements ModInitializer {
	public static final String MOD_ID = "obscure_revival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		//注册自定义属性
		CustomizedAttributes.register();
		//注册Fabric伤害后事件（举盾挡住时不激发战痕应激，无视blocked参数）
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
				KeloidEnchantmentManager.handleDamage(entity, source)
		);
		//将 KELOID_ARMOR 属性添加到玩家实体的默认属性容器，属性系统的 setTracked(true) 会自动处理客户端同步
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes().add(CustomizedAttributes.KELOID_ARMOR)
		);
		LOGGER.info("\"Obscure Revival\" loaded!");
	}
}