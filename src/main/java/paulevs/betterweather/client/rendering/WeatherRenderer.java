package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MCMath;
import net.minecraft.util.maths.Vec3D;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.config.CommonConfig;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class WeatherRenderer {
	private static final float TO_RADIANS = (float) (Math.PI / 180);
	private final float[] randomOffset;
	private final byte[] randomIndex;
	private int rainTexture;
	private int snowTexture;
	private int waterCircles;
	
	public WeatherRenderer() {
		randomOffset = new float[256];
		randomIndex = new byte[256];
		Random random = new Random(0);
		for (short i = 0; i < 256; i++) {
			randomOffset[i] = random.nextFloat();
			randomIndex[i] = (byte) random.nextInt(4);
		}
	}
	
	public void update(TextureManager manager) {
		this.rainTexture = manager.getTextureId("/environment/rain.png");
		this.snowTexture = manager.getTextureId("/environment/snow.png");
		this.waterCircles = manager.getTextureId("/assets/better_weather/textures/water_circles.png");
	}
	
	public void render(float delta, Minecraft minecraft) {
		LivingEntity entity = minecraft.viewEntity;
		double x = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, entity.prevRenderX, entity.x);
		double y = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, entity.prevRenderY, entity.y);
		double z = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, entity.prevRenderZ, entity.z);
		
		int ix = MCMath.floor(entity.x);
		int iy = MCMath.floor(entity.y);
		int iz = MCMath.floor(entity.z);
		
		int radius = minecraft.options.fancyGraphics ? 10 : 5;
		int radiusCenter = radius / 2 - 1;
		float sampleHeight = CommonConfig.useVanillaClouds() ? 2.5F : 8.5F;
		Level level = minecraft.level;
		int rainTop = (int) (level.dimension.getCloudHeight() + sampleHeight);
		
		if (iy - rainTop > 40) return;
		
		float vOffset = (float) (((double) level.getLevelTime() + delta) * 0.05 % 1.0);
		Vec3D pos = getPosition(entity);
		Vec3D dir = getViewDirection(entity);
		
		Tessellator tessellator = Tessellator.INSTANCE;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, rainTexture);
		
		tessellator.start();
		tessellator.setOffset(-x, -y, -z);
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = (ix & -4) + (dx << 2);
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				if (Math.abs(dx) < radiusCenter && Math.abs(dz) < radiusCenter) continue;
				int wz = (iz & -4) + (dz << 2);
				renderLargeSection(level, wx, iy, wz, pos, dir, rainTop, tessellator, vOffset, false);
			}
		}
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = ix + dx;
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				int wz = iz + dz;
				renderNormalSection(level, wx, iy, wz, pos, dir, rainTop, tessellator, vOffset, false);
			}
		}
		
		tessellator.render();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, waterCircles);
		vOffset = (float) (((double) level.getLevelTime() + delta) * 0.07 % 1.0);
		
		tessellator.start();
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = ix + dx;
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				int wz = iz + dz;
				renderWaterCircles(level, wx, iy, wz, pos, dir, tessellator, vOffset, radius);
			}
		}
		
		tessellator.render();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, snowTexture);
		vOffset = (float) (((double) level.getLevelTime() + delta) * 0.002 % 1.0);
		
		tessellator.start();
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = (ix & -4) + (dx << 2);
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				if (Math.abs(dx) < radiusCenter && Math.abs(dz) < radiusCenter) continue;
				int wz = (iz & -4) + (dz << 2);
				renderLargeSection(level, wx, iy, wz, pos, dir, rainTop, tessellator, vOffset, true);
			}
		}
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = ix + dx;
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				int wz = iz + dz;
				renderNormalSection(level, wx, iy, wz, pos, dir, rainTop, tessellator, vOffset, true);
			}
		}
		
		tessellator.setOffset(0.0, 0.0, 0.0);
		tessellator.render();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
	}
	
	private void renderLargeSection(Level level, int x, int y, int z, Vec3D pos, Vec3D dir, int rainTop, Tessellator tessellator, float vOffset, boolean snow) {
		int terrain = WeatherAPI.getRainHeight(level, x, z);
		if (terrain - y > 40) return;
		
		boolean visible = pointIsVisible(pos, dir, x + 0.5, terrain, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, y, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, rainTop, z + 0.5);
		if (!visible) return;
		
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		if (level.getBiomeSource().getBiome(x, z).canSnow() != snow) return;
		
		float v1 = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v2 = ((rainTop - terrain) * 0.0625F + v1);
		
		float light = level.getBrightness(x, terrain, z);
		float alpha = WeatherAPI.sampleFront(level, x, z, 0.1F);
		alpha = net.modificationstation.stationapi.api.util.math.MathHelper.clamp((alpha - 0.2F) * 2, 0.5F, 1F);
		tessellator.color(light, light, light, alpha);
		
		float u1 = ((x + z) & 3) * 0.25F;
		float u2 = u1 + 0.25F;
		
		float dx = (float) (pos.x - (x + 0.5));
		float dz = (float) (pos.z - (z + 0.5));
		float l = dx * dx + dz * dz;
		if (l > 0) {
			l = MCMath.sqrt(l) / 0.5F;
			dx /= l;
			dz /= l;
			float v = dx;
			dx = -dz;
			dz = v;
		}
		else {
			dx = 0.5F;
			dz = 0;
		}
		
		double x1 = x + 0.5 + dx;
		double x2 = x + 0.5 - dx;
		double z1 = z + 0.5 + dz;
		double z2 = z + 0.5 - dz;
		
		tessellator.vertex(x1, terrain, z1, u1, v1);
		tessellator.vertex(x1, rainTop, z1, u1, v2);
		tessellator.vertex(x2, rainTop, z2, u2, v2);
		tessellator.vertex(x2, terrain, z2, u2, v1);
	}
	
	private void renderNormalSection(Level level, int x, int y, int z, Vec3D pos, Vec3D dir, int rainTop, Tessellator tessellator, float vOffset, boolean snow) {
		int terrain = WeatherAPI.getRainHeight(level, x, z);
		if (terrain - y > 40) return;
		
		boolean visible = pointIsVisible(pos, dir, x + 0.5, terrain, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, y, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, rainTop, z + 0.5);
		if (!visible) return;
		
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		if (level.getBiomeSource().getBiome(x, z).canSnow() != snow) return;
		
		float v1 = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v2 = (rainTop - terrain) * 0.0625F + v1;
		
		float light = level.getBrightness(x, terrain, z);
		float alpha = WeatherAPI.sampleFront(level, x, z, 0.1F);
		alpha = net.modificationstation.stationapi.api.util.math.MathHelper.clamp((alpha - 0.2F) * 2, 0.5F, 1F);
		tessellator.color(light, light, light, alpha);
		
		float u1 = ((x + z) & 3) * 0.25F;
		float u2 = u1 + 0.25F;
		
		float dx = (float) (pos.x - (x + 0.5));
		float dz = (float) (pos.z - (z + 0.5));
		float l = dx * dx + dz * dz;
		if (l > 0) {
			l = MCMath.sqrt(l) / 0.5F;
			dx /= l;
			dz /= l;
			float v = dx;
			dx = -dz;
			dz = v;
		}
		else {
			dx = 0.5F;
			dz = 0;
		}
		
		double x1 = x + 0.5 + dx;
		double x2 = x + 0.5 - dx;
		double z1 = z + 0.5 + dz;
		double z2 = z + 0.5 - dz;
		
		tessellator.vertex(x1, terrain, z1, u1, v1);
		tessellator.vertex(x1, rainTop, z1, u1, v2);
		tessellator.vertex(x2, rainTop, z2, u2, v2);
		tessellator.vertex(x2, terrain, z2, u2, v1);
	}
	
	private void renderWaterCircles(Level level, int x, int y, int z, Vec3D pos, Vec3D dir, Tessellator tessellator, float vOffset, float radius) {
		int height = level.getHeight(x, z);
		if (height - y > 40 || y - height > 40) return;
		if (!pointIsVisible(pos, dir, x + 0.5, height, z + 0.5)) return;
		if (!level.getBlockState(x, height - 1, z).isOf(Block.STILL_WATER)) return;
		if (!WeatherAPI.isRaining(level, x, height, z)) return;
		
		float dx = (float) (x - pos.x);
		float dy = (float) (y - pos.y);
		float dz = (float) (z - pos.z);
		float alpha = 1F - MCMath.sqrt(dx * dx + dy * dy + dz * dz) / radius;
		alpha = alpha * 4F;
		if (alpha <= 0.01F) return;
		if (alpha > 1F) alpha = 1F;
		
		float light = level.getBrightness(x, height, z);
		
		float u1 = 0;
		float u2 = 1;
		vOffset += randomOffset[(x & 15) << 4 | (z & 15)];
		float v1 = MCMath.floor(vOffset * 6F) / 6F;
		float v2 = v1 + 1F / 6F;
		
		byte index = randomIndex[(x & 15) << 4 | (z & 15)];
		if ((index & 1) == 0) {
			u2 = 0;
			u1 = 1;
		}
		if (index > 1) {
			float value = v1;
			v1 = v2;
			v2 = value;
		}
		
		tessellator.color(light, light, light, alpha);
		tessellator.vertex(x, height, z, u1, v1);
		tessellator.vertex(x, height, z + 1, u1, v2);
		tessellator.vertex(x + 1, height, z + 1, u2, v2);
		tessellator.vertex(x + 1, height, z, u2, v1);
	}
	
	private Vec3D getPosition(LivingEntity entity) {
		return Vec3D.getFromCacheAndSet(entity.x, entity.y, entity.z);
	}
	
	private Vec3D getViewDirection(LivingEntity entity) {
		float yaw = entity.prevYaw + (entity.yaw - entity.prevYaw);
		float pitch = entity.prevPitch + (entity.pitch - entity.prevPitch);
		
		yaw = -yaw * TO_RADIANS - (float) Math.PI;
		float cosYaw = MCMath.cos(yaw);
		float sinYaw = MCMath.sin(yaw);
		float cosPitch = -MCMath.cos(-pitch * TO_RADIANS);
		
		return Vec3D.getFromCacheAndSet(
			sinYaw * cosPitch,
			(MCMath.sin(-pitch * ((float) Math.PI / 180))),
			cosYaw * cosPitch
		);
	}
	
	private boolean pointIsVisible(Vec3D position, Vec3D normal, double x, double y, double z) {
		return normal.x * (x - position.x) + normal.y * (y - position.y) + normal.z * (z - position.z) > 0;
	}
}
