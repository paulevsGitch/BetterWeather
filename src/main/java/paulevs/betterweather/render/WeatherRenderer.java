package paulevs.betterweather.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.api.WeatherAPI;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class WeatherRenderer {
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
		
		Tessellator tessellator = Tessellator.INSTANCE;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, rainTexture);
		
		tessellator.start();
		tessellator.setOffset(-x, -y, -z);
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = (ix & -4) + (dx << 2);
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				if (Math.abs(dx) < radiusCenter && Math.abs(dz) < radiusCenter) continue;
				int wz = (iz & -4) + (dz << 2);
				renderLargeSection(level, wx, iy, wz, rainTop, tessellator, vOffset);
			}
		}
		
		for (byte dx = (byte) -radius; dx <= radius; dx++) {
			int wx = ix + dx;
			for (byte dz = (byte) -radius; dz <= radius; dz++) {
				int wz = iz + dz;
				renderNormalSection(level, wx, iy, wz, rainTop, tessellator, vOffset);
			}
		}
		
		tessellator.setOffset(0.0, 0.0, 0.0);
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
	}
	
	private void renderLargeSection(Level level, int x, int y, int z, int rainTop, Tessellator tessellator, float vOffset) {
		int terrain = level.getHeight(x, z);
		if (terrain - y > 40) return;
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		
		float dv = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v1 = dv * 4F;
		float v2 = ((rainTop - terrain) * 0.0625F + dv) * 4F;
		
		float light = 1F;//level.getBrightness(x, terrain, z);
		GL11.glColor4f(light, light, light, 1F);
		
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
	
	private void renderNormalSection(Level level, int x, int y, int z, int rainTop, Tessellator tessellator, float vOffset) {
		int terrain = level.getHeight(x, z);
		
		if (terrain - y > 40) return;
		if (!WeatherAPI.isRaining(level, x, terrain, z)) return;
		
		float dv = randomOffset[(x & 15) << 4 | (z & 15)] + vOffset;
		float v1 = dv;
		float v2 = (rainTop - terrain) * 0.0625F + dv;
		
		float light = level.getBrightness(x, terrain, z);
		GL11.glColor4f(light, light, light, 1F);
		
		float u1 = (x & 3) * 0.25F;
		float u2 = u1 + 0.25F;
		
		tessellator.vertex(x, terrain, z + 0.5F, u1, v1);
		tessellator.vertex(x, rainTop, z + 0.5F, u1, v2);
		tessellator.vertex(x + 1, rainTop, z + 0.5F, u2, v2);
		tessellator.vertex(x + 1, terrain, z + 0.5F, u2, v1);
		
		u1 = (z & 3) * 0.25F;
		u2 = u1 + 0.25F;
		v1 += 0.5F;
		v2 += 0.5F;
		
		tessellator.vertex(x + 0.5F, terrain, z, u1, v1);
		tessellator.vertex(x + 0.5F, rainTop, z, u1, v2);
		tessellator.vertex(x + 0.5F, rainTop, z + 1, u2, v2);
		tessellator.vertex(x + 0.5F, terrain, z + 1, u2, v1);
	}
}
