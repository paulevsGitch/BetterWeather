package paulevs.betterweather.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import paulevs.betterweather.api.WeatherAPI;
import paulscode.sound.SoundSystem;

import java.net.URL;

@Environment(EnvType.CLIENT)
public class WeatherSounds {
	public static final SoundEntry RAIN_NORMAL = getSound("ambient/rain_normal");
	public static final SoundEntry RAIN_ROOF = getSound("ambient/rain_roof");
	
	private static final String RAIN_KEY = "ambient.weather.rain";
	private static boolean underRoof = false;
	
	public static SoundEntry getSound(String name) {
		name = "assets/better_weather/stationapi/sounds/" + name + ".ogg";
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
		return new SoundEntry(name, url);
	}
	
	public static void updateSound(Level level, LivingEntity entity, SoundSystem soundSystem) {
		if (level == null || entity == null) {
			if (soundSystem.playing(RAIN_KEY)) soundSystem.stop(RAIN_KEY);
			return;
		}
		
		float volume = WeatherAPI.getRainDensity(level, entity.x, entity.y, entity.z) * soundSystem.getMasterVolume();
		if (volume == 0) {
			if (soundSystem.playing(RAIN_KEY)) soundSystem.stop(RAIN_KEY);
			return;
		}
		
		boolean newRoof = entity.y < level.getHeight(MathHelper.floor(entity.x), MathHelper.floor(entity.z));
		if (newRoof != underRoof || !soundSystem.playing(RAIN_KEY)) {
			SoundEntry sound = newRoof ? RAIN_ROOF : RAIN_NORMAL;
			soundSystem.backgroundMusic(RAIN_KEY, sound.soundUrl, sound.soundName, true);
			soundSystem.play(RAIN_KEY);
			underRoof = newRoof;
		}
		
		soundSystem.setVolume(RAIN_KEY, volume);
	}
}
