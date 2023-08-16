package paulevs.betterweather.util;

import net.minecraft.entity.LightningEntity;
import net.minecraft.level.Level;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.config.CommonConfig;

public class LightningUtil {
	private static int lightningTicks;
	
	public static void tick() {
		lightningTicks = (lightningTicks + 1) & 31;
	}
	
	public static void processChunk(Level level, int cx, int cz) {
		if (lightningTicks > 0 || level.random.nextInt(1000) > 0) return;
		
		int px, py, pz;
		int lx = 0, ly = Integer.MIN_VALUE, lz = 0;
		
		short radius = CommonConfig.getRodCheckSide();
		for (short dx = (short) -radius; dx <= radius; dx++) {
			px = (cx << 4) + dx + 8;
			for (short dz = (short) -radius; dz <= radius; dz++) {
				pz = (cz << 4) + dz + 8;
				py = WeatherAPI.getRainHeight(level, px, pz);
				if (!WeatherAPI.isThundering(level, px, py, pz)) continue;
				if (!level.getBlockState(px, py - 1, pz).isIn(WeatherTags.LIGHTNING_ROD)) continue;
				lx = px;
				ly = py;
				lz = pz;
				break;
			}
		}
		
		if (ly == Integer.MIN_VALUE) {
			lx = (cx << 4) | level.random.nextInt(16);
			lz = (cz << 4) | level.random.nextInt(16);
			ly = WeatherAPI.getRainHeight(level, lx, lz);
			if (!WeatherAPI.isThundering(level, lx, ly, lz)) return;
		}
		
		level.addEntity(new LightningEntity(level, lx, ly, lz));
	}
}
