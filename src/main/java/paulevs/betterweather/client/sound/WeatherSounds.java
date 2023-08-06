package paulevs.betterweather.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundEntry;

import java.net.URL;

@Environment(EnvType.CLIENT)
public class WeatherSounds {
	public static final SoundEntry RAIN_NORMAL = getSound("ambient/rain_normal");
	public static final SoundEntry RAIN_ROOF = getSound("ambient/rain_roof");
	
	private static final String RAIN_KEY = "ambient.weather.rain";
	
	public static SoundEntry getSound(String name) {
		name = "assets/better_weather/stationapi/sounds/" + name + ".ogg";
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
		return new SoundEntry(name, url);
	}
}
