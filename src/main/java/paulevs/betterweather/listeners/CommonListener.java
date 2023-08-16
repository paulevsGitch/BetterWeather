package paulevs.betterweather.listeners;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.BaseBlock;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.registry.ModID;
import paulevs.betterweather.config.CommonConfig;
import paulevs.betterweather.util.LightningLightBlock;

public class CommonListener {
	public static BaseBlock lightningLight;
	
	@EventListener
	public void onInit(InitEvent event) {
		CommonConfig.init();
	}
	
	@EventListener
	public void onBlockRegister(BlockRegistryEvent event) {
		ModID modID = ModID.of("better_weather");
		lightningLight = new LightningLightBlock(modID.id("lightning_light"));
	}
}
