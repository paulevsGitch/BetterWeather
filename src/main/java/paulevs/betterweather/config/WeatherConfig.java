package paulevs.betterweather.config;

import java.io.File;

public class WeatherConfig {
	private static final Config CONFIG = new Config(new File("config/better_weather.cfg"));
	private static boolean useVanillaClouds;
	private static double cloudsSpeed;
	
	public static void init() {
		CONFIG.addEntry("useVanillaClouds", false,
			"Use vanilla clouds texture as a base map for clouds",
			"Default value is false"
		);
		CONFIG.addEntry("cloudsSpeed", 0.001F,
			"Clouds speed in ticks per chunk, larger values will cause clouds move faster",
			"Default value is 0.001"
		);
		CONFIG.save();
		
		useVanillaClouds = CONFIG.getBool("useVanillaClouds");
		cloudsSpeed = CONFIG.getFloat("cloudsSpeed");
	}
	
	public static boolean useVanillaClouds() {
		return useVanillaClouds;
	}
	
	public static double getCloudsSpeed() {
		return cloudsSpeed;
	}
}
