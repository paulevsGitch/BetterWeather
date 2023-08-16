package paulevs.betterweather.config;

import java.io.File;

public class CommonConfig {
	private static final Config CONFIG = new Config(new File("config/better_weather/common.cfg"));
	private static boolean useVanillaClouds;
	private static double cloudsSpeed;
	private static boolean eternalRain;
	private static boolean eternalThunder;
	private static boolean frequentRain;
	private static int rodCheckSide;
	private static int lightningChance;
	
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
		CONFIG.addEntry("eternalThunder", false,
			"Makes weather in the whole world thunderstorm",
			"Default value is false"
		);
		CONFIG.addEntry("frequentRain", false,
			"Makes rain more frequent instead of vanilla behaviour",
			"Default value is false"
		);
		CONFIG.addEntry("rodCheckSide", 32,
			"Distance in blocks (from the rod block) that will be protected from lightnings",
			"The area is square with center on rod block and radius equal to this number",
			"Max value is " + Short.MAX_VALUE + " and min is 0",
			"Default value is 32"
		);
		CONFIG.addEntry("lightningChance", 300,
			"Chance that lighting will happen in this chunk (during thunderstorm)",
			"The actual chance is calculated as 1/lightningChance",
			"Smaller values will result with more lighting and visa versa",
			"Max value is " + Short.MAX_VALUE + " and min is 1",
			"Default value is 300"
		);
		CONFIG.save();
		
		useVanillaClouds = CONFIG.getBool("useVanillaClouds");
		cloudsSpeed = CONFIG.getFloat("cloudsSpeed");
		eternalRain = CONFIG.getBool("eternalRain");
		eternalThunder = CONFIG.getBool("eternalThunder");
		frequentRain = CONFIG.getBool("frequentRain");
		rodCheckSide = CONFIG.getInt("rodCheckSide");
		lightningChance = CONFIG.getInt("lightningChance");
		
		if (rodCheckSide > Short.MAX_VALUE) rodCheckSide = Short.MAX_VALUE;
		if (rodCheckSide < 0) rodCheckSide = 0;
		
		if (lightningChance > Short.MAX_VALUE) lightningChance = Short.MAX_VALUE;
		if (lightningChance < 1) rodCheckSide = 1;
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
	
	public static boolean isEternalThunder() {
		return eternalThunder;
	}
	
	public static boolean isFrequentRain() {
		return frequentRain;
	}
	
	public static short getRodCheckSide() {
		return (short) rodCheckSide;
	}
	
	public static int getLightningChance() {
		return lightningChance;
	}
}
