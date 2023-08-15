package paulevs.betterweather.listeners;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import paulevs.betterweather.config.ClientConfig;

@Environment(EnvType.CLIENT)
public class ClientListener {
	@EventListener
	public void onInit(InitEvent event) {
		ClientConfig.init();
	}
}
