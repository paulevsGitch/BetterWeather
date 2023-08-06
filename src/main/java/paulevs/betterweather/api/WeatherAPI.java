package paulevs.betterweather.api;

import net.minecraft.level.Level;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import paulevs.betterweather.render.ImageSampler;

public class WeatherAPI {
	private static final ImageSampler MAIN_SHAPE_SAMPLER = new ImageSampler("assets/better_weather/textures/main_shape.png");
	private static final ImageSampler LARGE_DETAILS_SAMPLER = new ImageSampler("assets/better_weather/textures/large_details.png");
	private static final ImageSampler VARIATION_SAMPLER = new ImageSampler("assets/better_weather/textures/variation.png");
	private static final ImageSampler FRONTS_SAMPLER = new ImageSampler("assets/better_weather/textures/rain_fronts.png");
	private static final float[] CLOUD_SHAPE = new float[16];
	
	public static final double CLOUDS_SPEED = 0.001; // Chunks per tick
	
	public static boolean isRaining(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return false;
		if (y > level.dimension.getCloudHeight() + 8) return false;
		
		z -= ((double) level.getLevelTime()) * CLOUDS_SPEED * 32;
		
		float rainFront = sampleFront(x, z, 0.1);
		if (rainFront <= 0.5F) return false;
		
		float coverage = getCoverage(rainFront);
		return getCloudDensity(x, 5, z) > coverage &&
			getCloudDensity(x - 4, 5, z) > coverage &&
			getCloudDensity(x + 4, 5, z) > coverage &&
			getCloudDensity(x, 5, z - 4) > coverage &&
			getCloudDensity(x, 5, z + 4) > coverage;
	}
	
	public static boolean isInCloud(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return false;
		int start = (int) level.dimension.getCloudHeight();
		if (y < start || y > start + 31) return false;
		float rainFront = sampleFront(x, z, 0.1);
		float coverage = getCoverage(rainFront);
		return getCloudDensity(x, y - start, z) < coverage;
	}
	
	public static float getCloudDensity(int x, int y, int z) {
		float density = MAIN_SHAPE_SAMPLER.sample(x * 0.75F, z * 0.75F);
		density += LARGE_DETAILS_SAMPLER.sample(x * 2.5F, z * 2.5F);
		
		density -= VARIATION_SAMPLER.sample(y * 2.5F, x * 2.5F) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 2.5F, y * 2.5F) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 2.5F, x * 2.5F) * 0.05F;
		
		int value = (int) (MathHelper.hashCode(x, y, z) % 3);
		density -= value * 0.01F;
		
		return density - CLOUD_SHAPE[y >> 1];
	}
	
	public static float sampleFront(int x, int z, double scale) {
		return FRONTS_SAMPLER.sample(x * scale, z * scale);
	}
	
	public static float getCoverage(float rainFront) {
		return MathHelper.lerp(rainFront, 1.3F, 0.5F);
	}
	
	static {
		for (byte i = 0; i < 4; i++) {
			CLOUD_SHAPE[i] = (4 - i) / 4F;
			CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
		}
		for (byte i = 4; i < 16; i++) {
			CLOUD_SHAPE[i] = (i - 4) / 12F;
			CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
		}
	}
}
