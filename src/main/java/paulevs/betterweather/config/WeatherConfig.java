package paulevs.betterweather.config;

import java.io.File;

public class WeatherConfig {
	private static final Config CONFIG = new Config(new File("config/better_weather.cfg"));
	private static boolean useVanillaClouds;
	private static double cloudsSpeed;
	private static boolean eternalRain;
	private static boolean frequentRain;
	
	public static void init() {
		CONFIG.addEntry("useVanillaClouds", false,
			"Use vanilla clouds texture as a base map for clouds",
			"Default value is false"
		);
		CONFIG.addEntry("cloudsSpeed", 0.001F,
			"Clouds speed in ticks per chunk, larger values will cause clouds move faster",
			"Default value is 0.001"
		);
		CONFIG.addEntry("eternalRain", false,
			"Makes weather in the whole world rain only",
			"Default value is false"
		);
		CONFIG.addEntry("frequentRain", false,
			"Makes rain more frequent instead of vanilla behaviour",
			"Default value is false"
		);
		CONFIG.save();
		
		useVanillaClouds = CONFIG.getBool("useVanillaClouds");
		cloudsSpeed = CONFIG.getFloat("cloudsSpeed");
		eternalRain = CONFIG.getBool("eternalRain");
		frequentRain = CONFIG.getBool("frequentRain");
	}
	
	public static boolean useVanillaClouds() {
		return useVanillaClouds;
	}
	
	public static double getCloudsSpeed() {
		return cloudsSpeed;
	}
	
	public static boolean isEternalRain() {
		return eternalRain;
	}
	
	public static boolean isFrequentRain() {
		return frequentRain;
	}
}
