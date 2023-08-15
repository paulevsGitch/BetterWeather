package paulevs.betterweather.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ClientConfig {
	private static final Config CONFIG = new Config(new File("config/better_weather/client.cfg"));
	private static boolean fluffyClouds;
	
	public static void init() {
		CONFIG.addEntry("fluffyClouds", true,
			"Render clouds fluffy, if false clouds will use block-like rendering",
			"Default value is true"
		);
		CONFIG.save();
		
		fluffyClouds = CONFIG.getBool("fluffyClouds");
	}
	
	public static boolean isFluffyClouds() {
		return fluffyClouds;
	}
}
