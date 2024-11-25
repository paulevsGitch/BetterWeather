package paulevs.betterweather.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.util.maths.MCMath;
import net.minecraft.util.maths.Vec2I;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.registry.DimensionContainer;
import net.modificationstation.stationapi.api.registry.DimensionRegistry;
import net.modificationstation.stationapi.api.registry.RegistryEntry;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import paulevs.betterweather.config.CommonConfig;
import paulevs.betterweather.util.ImageSampler;
import paulevs.betterweather.util.WeatherTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeatherAPI {
	private static final ImageSampler MAIN_SHAPE_SAMPLER = new ImageSampler("data/better_weather/clouds/main_shape.png");
	private static final ImageSampler LARGE_DETAILS_SAMPLER = new ImageSampler("data/better_weather/clouds/large_details.png");
	private static final ImageSampler VARIATION_SAMPLER = new ImageSampler("data/better_weather/clouds/variation.png");
	private static final ImageSampler FRONTS_SAMPLER = new ImageSampler("data/better_weather/clouds/rain_fronts.png");
	private static final ImageSampler RAIN_DENSITY = new ImageSampler("data/better_weather/clouds/rain_density.png");
	private static final ImageSampler VANILLA_CLOUDS = new ImageSampler("data/better_weather/clouds/vanilla_clouds.png").setSmooth(true);
	private static final ImageSampler THUNDERSTORMS = new ImageSampler("data/better_weather/clouds/thunderstorms.png");
	private static final float[] CLOUD_SHAPE = new float[64];
	private static final Vec2I[] OFFSETS;
	
	public static boolean isRaining(Level level, int x, int y, int z) {
		if (level.dimension.evaporatesWater) return false;
		
		RegistryEntry<DimensionContainer<?>> dimension = getDimension(level);
		if (dimension != null && dimension.isIn(WeatherTags.NO_RAIN)) return false;
		
		if (y > getCloudHeight(level.dimension) + 8) return false;
		if (y < getRainHeight(level, x, z)) return false;
		
		z = (int) (z - level.getLevelTime() * CommonConfig.getCloudsSpeed() * 32);
		if (CommonConfig.isEternalRain() || (dimension != null && dimension.isIn(WeatherTags.ETERNAL_RAIN))) {
			return !CommonConfig.useVanillaClouds() || getCloudDensity(x, 2, z, 1F) > 0.5F;
		}
		
		float rainFront = sampleFront(level, x, z, 0.1);
		if (rainFront < 0.2F) return false;
		
		float coverage = getCoverage(rainFront);
		int sampleHeight = CommonConfig.useVanillaClouds() ? 2 : 7;
		return getCloudDensity(x, sampleHeight, z, rainFront) > coverage;
	}
	
	public static boolean isThundering(Level level, int x, int y, int z) {
		return isRaining(level, x, y, z) && sampleThunderstorm(level, x, z, 0.05) > 0.3F;
	}
	
	public static float inCloud(Level level, double x, double y, double z) {
		z -= level.getLevelTime() * CommonConfig.getCloudsSpeed() * 32;
		int x1 = MCMath.floor(x / 2.0) << 1;
		int y1 = MCMath.floor(y / 2.0) << 1;
		int z1 = MCMath.floor(z / 2.0) << 1;
		
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
		int start = (int) getCloudHeight(level.dimension);
		if (y < start || y > start + 64) return false;
		float rainFront = sampleFront(level, x, z, 0.1);
		float coverage = getCoverage(rainFront);
		return getCloudDensity(x, y - start, z, rainFront) > coverage;
	}
	
	public static float getCloudDensity(int x, int y, int z, float rainFront) {
		if (CommonConfig.useVanillaClouds()) {
			if (y > 6) return 0;
			float shape = y == 0 || y == 5 ? 1 : 0;
			return VANILLA_CLOUDS.sample(x / 16.0, z / 16.0) * 3 - shape;
		}
		
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
	
	public static float sampleFront(Level level, int x, int z, double scale) {
		if (CommonConfig.isEternalRain()) return 1F;
		
		RegistryEntry<DimensionContainer<?>> dimension = getDimension(level);
		if (dimension != null) {
			if (dimension.isIn(WeatherTags.NO_RAIN)) return 0F;
			if (dimension.isIn(WeatherTags.ETERNAL_RAIN)) return 1F;
		}
		
		float front = FRONTS_SAMPLER.sample(x * scale, z * scale);
		if (!CommonConfig.isFrequentRain()) {
			scale *= 0.7;
			front *= RAIN_DENSITY.sample(x * scale, z * scale);
		}
		return front;
	}
	
	public static float sampleThunderstorm(Level level, int x, int z, double scale) {
		if (CommonConfig.isEternalThunder()) return 1F;
		
		RegistryEntry<DimensionContainer<?>> dimension = getDimension(level);
		if (dimension != null) {
			if (dimension.isIn(WeatherTags.NO_THUNDER)) return 0F;
			if (dimension.isIn(WeatherTags.ETERNAL_THUNDER)) return 1F;
		}
		
		return THUNDERSTORMS.sample(x * scale, z * scale);
	}
	
	public static float getCoverage(float rainFront) {
		return MathHelper.lerp(rainFront, 1.3F, 0.5F);
	}
	
	public static int getRainHeight(Level level, int x, int z) {
		int max = (int) (getCloudHeight(level.dimension) + 4);
		int height = level.getHeight(x, z);
		if (height >= max) return max;
		Chunk chunk = level.getChunkFromCache(x >> 4, z >> 4);
		x &= 15;
		z &= 15;
		for (int y = max; y > height; y--) {
			BlockState state = chunk.getBlockState(x, y, z);
			if (state.isAir()) continue;
			if (state.isOpaque() || state.getBlock().isFullCube() || state.getMaterial().isLiquid()) return y + 1;
		}
		return height;
	}
	
	public static float getRainDensity(Level level, double x, double y, double z, boolean includeSnow) {
		int x1 = MCMath.floor(x);
		int y1 = MCMath.floor(y);
		int z1 = MCMath.floor(z);
		int x2 = x1 + 1;
		int z2 = z1 + 1;
		
		float dx = (float) (x - x1);
		float dz = (float) (z - z1);
		dz -= (float) ((level.getLevelTime() * CommonConfig.getCloudsSpeed() * 32) % 1.0);
		
		float a = getRainDensity(level, x1, y1, z1, includeSnow);
		float b = getRainDensity(level, x2, y1, z1, includeSnow);
		float c = getRainDensity(level, x1, y1, z2, includeSnow);
		float d = getRainDensity(level, x2, y1, z2, includeSnow);
		
		float value = MathHelper.interpolate2D(dx, dz, a, b, c, d);
		return MathHelper.clamp(value, 0F, 1F);
	}
	
	private static float getRainDensity(Level level, int x, int y, int z, boolean includeSnow) {
		if (level.dimension.evaporatesWater) return 0;
		
		int count = 0;
		for (Vec2I offset : OFFSETS) {
			boolean snowCheck = includeSnow || !level.getBiomeSource().getBiome(x, z).canSnow();
			if (snowCheck && isRaining(level, x + offset.x, y, z + offset.z)) {
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
		
		List<Vec2I> offsets = new ArrayList<>(capacity);
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (x * x + z * z <= radius * radius) {
					offsets.add(new Vec2I(x, z));
				}
			}
		}
		offsets.sort((v1, v2) -> {
			int d1 = v1.x * v1.x + v1.z * v1.z;
			int d2 = v2.x * v2.x + v2.z * v2.z;
			return Integer.compare(d1, d2);
		});
		OFFSETS = offsets.toArray(Vec2I[]::new);
	}
	
	private static RegistryEntry<DimensionContainer<?>> getDimension(Level level) {
		Optional<DimensionContainer<?>> optional = DimensionRegistry.INSTANCE.getByLegacyId(level.dimension.id);
		return optional.map(DimensionRegistry.INSTANCE::getEntry).orElse(null);
	}
	
	private static float getCloudHeight(Dimension dimension) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return dimension.getCloudHeight();
		}
		return 108F;
	}
}
