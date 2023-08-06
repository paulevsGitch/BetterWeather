package paulevs.betterweather.api;

import net.minecraft.level.Level;
import net.minecraft.util.maths.Vec2i;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import paulevs.betterweather.util.ImageSampler;

import java.util.ArrayList;
import java.util.List;

public class WeatherAPI {
	private static final ImageSampler MAIN_SHAPE_SAMPLER = new ImageSampler("assets/better_weather/textures/main_shape.png");
	private static final ImageSampler LARGE_DETAILS_SAMPLER = new ImageSampler("assets/better_weather/textures/large_details.png");
	private static final ImageSampler VARIATION_SAMPLER = new ImageSampler("assets/better_weather/textures/variation.png");
	private static final ImageSampler FRONTS_SAMPLER = new ImageSampler("assets/better_weather/textures/rain_fronts.png");
	private static final float[] CLOUD_SHAPE = new float[64];
	private static final Vec2i[] OFFSETS;
	
	public static final double CLOUDS_SPEED = 0.001; // Chunks per tick
	
	public static boolean isRaining(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return false;
		if (y > level.dimension.getCloudHeight() + 8) return false;
		if (y < level.getHeight(x, z)) return false;
		
		z -= ((double) level.getLevelTime()) * CLOUDS_SPEED * 32;
		
		float rainFront = sampleFront(x, z, 0.1);
		if (rainFront < 0.2F) return false;
		
		float coverage = getCoverage(rainFront);
		return getCloudDensity(x, 7, z, rainFront) > coverage;
	}
	
	public static float inCloud(Level level, double x, double y, double z) {
		z -= ((double) level.getLevelTime()) * CLOUDS_SPEED * 32;
		int x1 = net.minecraft.util.maths.MathHelper.floor(x / 2.0) << 1;
		int y1 = net.minecraft.util.maths.MathHelper.floor(y / 2.0) << 1;
		int z1 = net.minecraft.util.maths.MathHelper.floor(z / 2.0) << 1;
		
		int x2 = x1 + 2;
		int y2 = y1 + 2;
		int z2 = z1 + 2;
		
		float dx = (float) (x - x1) / 2F;
		float dy = (float) (y - y1) / 2F;
		float dz = (float) (z - z1) / 2F;
		
		float a = isInCloud(level, x1, y1, z1) ? 1F : 0F;
		float b = isInCloud(level, x2, y1, z1) ? 1F : 0F;
		float c = isInCloud(level, x1, y2, z1) ? 1F : 0F;
		float d = isInCloud(level, x2, y2, z1) ? 1F : 0F;
		float e = isInCloud(level, x1, y1, z2) ? 1F : 0F;
		float f = isInCloud(level, x2, y1, z2) ? 1F : 0F;
		float g = isInCloud(level, x1, y2, z2) ? 1F : 0F;
		float h = isInCloud(level, x2, y2, z2) ? 1F : 0F;
		
		return MathHelper.interpolate3D(dx, dy, dz, a, b, c, d, e, f, g, h);
	}
	
	private static boolean isInCloud(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return false;
		int start = (int) level.dimension.getCloudHeight();
		if (y < start || y > start + 64) return false;
		float rainFront = sampleFront(x, z, 0.1);
		float coverage = getCoverage(rainFront);
		return getCloudDensity(x, y - start, z, rainFront) > coverage;
	}
	
	public static float getCloudDensity(int x, int y, int z, float rainFront) {
		float density = MAIN_SHAPE_SAMPLER.sample(x * 0.75F, z * 0.75F);
		density += LARGE_DETAILS_SAMPLER.sample(x * 2.5F, z * 2.5F);
		
		density -= VARIATION_SAMPLER.sample(y * 2.5F, x * 2.5F) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 2.5F, y * 2.5F) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 2.5F, x * 2.5F) * 0.05F;
		
		int value = (int) (MathHelper.hashCode(x, y, z) % 3);
		density -= value * 0.01F;
		
		float density1 = density - CLOUD_SHAPE[MathHelper.clamp(y << 1, 0, 63)];
		float density2 = density + MAIN_SHAPE_SAMPLER.sample(x * 1.5F, z * 1.5F) - CLOUD_SHAPE[MathHelper.clamp(y, 0, 63)] * 3F;
		
		return MathHelper.lerp(rainFront, density1, density2);
	}
	
	public static float sampleFront(int x, int z, double scale) {
		return FRONTS_SAMPLER.sample(x * scale, z * scale);
	}
	
	public static float getCoverage(float rainFront) {
		return MathHelper.lerp(rainFront, 1.3F, 0.5F);
	}
	
	public static float getRainDensity(Level level, double x, double y, double z) {
		int x1 = net.minecraft.util.maths.MathHelper.floor(x);
		int y1 = net.minecraft.util.maths.MathHelper.floor(y);
		int z1 = net.minecraft.util.maths.MathHelper.floor(z);
		int x2 = x1 + 1;
		int z2 = z1 + 1;
		
		float dx = (float) (x - x1);
		float dz = (float) (z - z1);
		
		float a = getRainDensity(level, x1, y1, z1);
		float b = getRainDensity(level, x2, y1, z1);
		float c = getRainDensity(level, x1, y1, z2);
		float d = getRainDensity(level, x2, y1, z2);
		
		return MathHelper.interpolate2D(dx, dz, a, b, c, d);
	}
	
	public static float getRainDensity(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return 0;
		
		int count = 0;
		for (Vec2i offset : OFFSETS) {
			if (isRaining(level, x + offset.x, y, z + offset.z)) {
				count++;
				if (count >= 64) return 1F;
			}
		}
		
		return count / 64F;
	}
	
	static {
		for (byte i = 0; i < 16; i++) {
			CLOUD_SHAPE[i] = (16 - i) / 16F;
			CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
		}
		for (byte i = 16; i < 64; i++) {
			CLOUD_SHAPE[i] = (i - 16) / 48F;
			CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
		}
		
		int radius = 6;
		int capacity = radius * 2 + 1;
		capacity *= capacity;
		
		List<Vec2i> offsets = new ArrayList<>(capacity);
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (x * x + z * z <= radius * radius) {
					offsets.add(new Vec2i(x, z));
				}
			}
		}
		offsets.sort((v1, v2) -> {
			int d1 = v1.x * v1.x + v1.z * v1.z;
			int d2 = v2.x * v2.x + v2.z * v2.z;
			return Integer.compare(d1, d2);
		});
		OFFSETS = offsets.toArray(Vec2i[]::new);
	}
}
