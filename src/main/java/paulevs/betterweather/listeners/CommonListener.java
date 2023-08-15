package paulevs.betterweather.listeners;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import paulevs.betterweather.config.CommonConfig;

public class CommonListener {
	@EventListener
	public void onInit(InitEvent event) {
		CommonConfig.init();
	}
}
