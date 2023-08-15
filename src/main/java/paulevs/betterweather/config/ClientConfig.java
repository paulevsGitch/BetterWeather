package paulevs.betterweather.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;

import java.io.File;

@Environment(EnvType.CLIENT)
public class ClientConfig {
	private static final Config CONFIG = new Config(new File("config/better_weather/client.cfg"));
	private static boolean fluffyClouds;
	private static GameOptions options;
	
	public static void init() {
		CONFIG.addEntry("fluffyClouds", true,
			"Render clouds fluffy, if false clouds will use block-like rendering",
			"Default value is true"
		);
		CONFIG.save();
		
		fluffyClouds = CONFIG.getBool("fluffyClouds");
	}
	
	@SuppressWarnings("deprecation")
	public static boolean renderFluffy() {
		if (options == null) {
			options = ((Minecraft) FabricLoader.getInstance().getGameInstance()).options;
		}
		return fluffyClouds && options.fancyGraphics;
	}
}
