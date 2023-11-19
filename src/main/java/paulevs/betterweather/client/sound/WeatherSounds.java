package paulevs.betterweather.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import paulevs.betterweather.api.WeatherAPI;
import paulscode.sound.SoundSystem;

import java.net.URL;

@Environment(EnvType.CLIENT)
public class WeatherSounds {
	public static final SoundEntry RAIN = getSound("ambient/rain");
	private static final String RAIN_KEY = "ambient.weather.rain";
	
	private static boolean underRoof = false;
	private static SoundSystem soundSystem;
	
	public static void init(SoundSystem soundSystem) {
		WeatherSounds.soundSystem = soundSystem;
	}
	
	public static SoundEntry getSound(String name) {
		name = "assets/better_weather/stationapi/sounds/" + name + ".ogg";
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
		return new SoundEntry(name, url);
	}
	
	public static void stop() {
		if (soundSystem == null) return;
		if (soundSystem.playing(RAIN_KEY)) soundSystem.stop(RAIN_KEY);
	}
	
	public static void updateSound(Level level, LivingEntity entity, SoundSystem soundSystem, float volume) {
		if (level == null || entity == null) {
			stop();
			return;
		}
		
		volume *= WeatherAPI.getRainDensity(level, entity.x, entity.y, entity.z, false) * 0.5F;
		if (volume == 0) {
			stop();
			return;
		}
		else if (!soundSystem.playing(RAIN_KEY)) {
			soundSystem.backgroundMusic(RAIN_KEY, RAIN.soundUrl, RAIN.soundName, true);
			soundSystem.play(RAIN_KEY);
		}
		
		boolean newRoof = entity.y < WeatherAPI.getRainHeight(level, MathHelper.floor(entity.x), MathHelper.floor(entity.z));
		if (newRoof != underRoof) {
			soundSystem.setPitch(RAIN_KEY, newRoof ? 0.25F : 1.0F);
			underRoof = newRoof;
		}
		
		soundSystem.setVolume(RAIN_KEY, volume);
	}
}
