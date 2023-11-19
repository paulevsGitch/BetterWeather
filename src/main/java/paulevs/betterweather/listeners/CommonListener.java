package paulevs.betterweather.listeners;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.util.Namespace;
import paulevs.betterweather.config.CommonConfig;
import paulevs.betterweather.util.LightningLightBlock;

public class CommonListener {
	public static Block lightningLight;
	
	@EventListener
	public void onInit(InitEvent event) {
		CommonConfig.init();
	}
	
	@EventListener
	public void onBlockRegister(BlockRegistryEvent event) {
		Namespace modID = Namespace.of("better_weather");
		lightningLight = new LightningLightBlock(modID.id("lightning_light"));
	}
}
