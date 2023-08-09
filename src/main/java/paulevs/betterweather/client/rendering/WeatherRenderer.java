package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec3f;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.api.WeatherAPI;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class WeatherRenderer {
	private static final float TO_RADIANS = (float) (Math.PI / 180);
	private final float[] randomOffset;
	private int rainTexture;
	private int snowTexture;
	
	public WeatherRenderer() {
		randomOffset = new float[256];
		Random random = new Random(0);
		for (short i = 0; i < 256; i++) {
			randomOffset[i] = random.nextFloat();
		}
	}
	
	public void update(TextureManager manager) {
		this.rainTexture = manager.getTextureId("/environment/rain.png");
		this.snowTexture = manager.getTextureId("/environment/snow.png");
	}
	
	public void render(float delta, Level level, LivingEntity viewEntity, boolean fancyGraphics) {
		double x = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, viewEntity.prevRenderX, viewEntity.x);
		double y = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, viewEntity.prevRenderY, viewEntity.y);
		double z = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(delta, viewEntity.prevRenderZ, viewEntity.z);
		
		int ix = MathHelper.floor(viewEntity.x);
		int iy = MathHelper.floor(viewEntity.y);
		int iz = MathHelper.floor(viewEntity.z);
		
		int radius = fancyGraphics ? 10 : 5;
		int radiusCenter = radius / 2 - 1;
		int rainTop = (int) (level.dimension.getCloudHeight() + 8.5F);
		
		if (iy - rainTop > 40) return;
		
		float vOffset = (float) (((double) level.getLevelTime() + delta) * 0.05 % 1.0);
		Vec3f pos = getPosition(viewEntity);
		Vec3f dir = getViewDirection(viewEntity);
		
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
		
		tessellator.setOffset(0.0, 0.0, 0.0);
		tessellator.draw();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, snowTexture);
		vOffset = (float) (((double) level.getLevelTime() + delta) * 0.002 % 1.0);
		
		tessellator.start();
		tessellator.setOffset(-x, -y, -z);
		
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
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
	}
	
	private void renderLargeSection(Level level, int x, int y, int z, Vec3f pos, Vec3f dir, int rainTop, Tessellator tessellator, float vOffset, boolean snow) {
		int terrain = level.getHeight(x, z);
		if (terrain - y > 40) return;
		
		boolean visible = pointIsVisible(pos, dir, x + 0.5, terrain, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, y, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, rainTop, z + 0.5);
		if (!visible) return;
		
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		if (level.getBiomeSource().getBiome(x, z).canSnow() != snow) return;
		
		float dv = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v1 = dv;
		float v2 = ((rainTop - terrain) * 0.0625F + dv);
		
		float light = level.getBrightness(x, terrain, z);
		float alpha = WeatherAPI.sampleFront(x, z, 0.1F);
		alpha = net.modificationstation.stationapi.api.util.math.MathHelper.clamp((alpha - 0.2F) * 2, 0.5F, 1F);
		tessellator.color(light, light, light, alpha);
		
		float u1 = (x & 3);
		float u2 = u1 + 1;
		
		tessellator.vertex(x - 1.5F, terrain, z + 0.5F, u1, v1);
		tessellator.vertex(x - 1.5F, rainTop, z + 0.5F, u1, v2);
		tessellator.vertex(x + 2.5F, rainTop, z + 0.5F, u2, v2);
		tessellator.vertex(x + 2.5F, terrain, z + 0.5F, u2, v1);
		
		u1 = (z & 3);
		u2 = u1 + 1;
		v1 += 0.5F;
		v2 += 0.5F;
		
		tessellator.vertex(x + 0.5F, terrain, z - 1.5F, u1, v1);
		tessellator.vertex(x + 0.5F, rainTop, z - 1.5F, u1, v2);
		tessellator.vertex(x + 0.5F, rainTop, z + 2.5F, u2, v2);
		tessellator.vertex(x + 0.5F, terrain, z + 2.5F, u2, v1);
	}
	
	private void renderNormalSection(Level level, int x, int y, int z, Vec3f pos, Vec3f dir, int rainTop, Tessellator tessellator, float vOffset, boolean snow) {
		int terrain = level.getHeight(x, z);
		if (terrain - y > 40) return;
		
		boolean visible = pointIsVisible(pos, dir, x + 0.5, terrain, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, y, z + 0.5);
		visible |= pointIsVisible(pos, dir, x + 0.5, rainTop, z + 0.5);
		if (!visible) return;
		
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		if (level.getBiomeSource().getBiome(x, z).canSnow() != snow) return;
		
		float dv = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v1 = dv;
		float v2 = (rainTop - terrain) * 0.0625F + dv;
		
		float light = level.getBrightness(x, terrain, z);
		float alpha = WeatherAPI.sampleFront(x, z, 0.1F);
		alpha = net.modificationstation.stationapi.api.util.math.MathHelper.clamp((alpha - 0.2F) * 2, 0.5F, 1F);
		tessellator.color(light, light, light, alpha);
		
		float u1 = (x & 3) * 0.25F;
		float u2 = u1 + 0.25F;
		
		tessellator.vertex(x, terrain, z + 0.5, u1, v1);
		tessellator.vertex(x, rainTop, z + 0.5, u1, v2);
		tessellator.vertex(x + 1, rainTop, z + 0.5, u2, v2);
		tessellator.vertex(x + 1, terrain, z + 0.5, u2, v1);
		
		u1 = (z & 3) * 0.25F;
		u2 = u1 + 0.25F;
		v1 += 0.5F;
		v2 += 0.5F;
		
		tessellator.vertex(x + 0.5F, terrain, z, u1, v1);
		tessellator.vertex(x + 0.5F, rainTop, z, u1, v2);
		tessellator.vertex(x + 0.5F, rainTop, z + 1, u2, v2);
		tessellator.vertex(x + 0.5F, terrain, z + 1, u2, v1);
	}
	
	private Vec3f getPosition(LivingEntity entity) {
		return Vec3f.getFromCacheAndSet(entity.x, entity.y, entity.z);
	}
	
	private Vec3f getViewDirection(LivingEntity entity) {
		float yaw = entity.prevYaw + (entity.yaw - entity.prevYaw);
		float pitch = entity.prevPitch + (entity.pitch - entity.prevPitch);
		
		yaw = -yaw * TO_RADIANS - (float) Math.PI;
		float cosYaw = MathHelper.cos(yaw);
		float sinYaw = MathHelper.sin(yaw);
		float cosPitch = -MathHelper.cos(-pitch * TO_RADIANS);
		
		return Vec3f.getFromCacheAndSet(
			sinYaw * cosPitch,
			(MathHelper.sin(-pitch * ((float) Math.PI / 180))),
			cosYaw * cosPitch
		);
	}
	
	private boolean pointIsVisible(Vec3f position, Vec3f normal, double x, double y, double z) {
		return normal.x * (x - position.x) + normal.y * (y - position.y) + normal.z * (z - position.z) > 0;
	}
}
