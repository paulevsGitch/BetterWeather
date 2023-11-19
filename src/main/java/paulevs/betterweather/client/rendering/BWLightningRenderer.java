package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.technical.LightningEntity;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.config.CommonConfig;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class BWLightningRenderer {
	private static final Random RANDOM = new Random(0);
	private static int texture = -1;
	
	public static void render(LightningEntity entity, float x, float y, float z, TextureManager manager) {
		if (texture == -1) {
			texture = manager.getTextureId("/assets/better_weather/textures/lightning.png");
		}
		
		float y2 = CommonConfig.useVanillaClouds() ? 2.5F : 8.5F;
		y2 += entity.level.dimension.getCloudHeight();
		y2 = (float) (y2 - entity.y) + y;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float dx = x;
		float dz = z;
		float l = dx * dx + dz * dz;
		if (l > 0) {
			l = MathHelper.sqrt(l) / 0.5F;
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
		
		float x1 = x + 0.5F + dx;
		float x2 = x + 0.5F - dx;
		float z1 = z + 0.5F + dz;
		float z2 = z + 0.5F - dz;
		float x1_2 = x + 0.5F + dx * 0.5F;
		float x2_2 = x + 0.5F - dx * 0.5F;
		float z1_2 = z + 0.5F + dz * 0.5F;
		float z2_2 = z + 0.5F - dz * 0.5F;
		
		int sectionCount = MathHelper.floor((y2 - y) / 8F + 1);
		float secDelta = (y2 - y) / sectionCount;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		tessellator.start();
		tessellator.color(255, 255, 255, 255);
		
		float dx1 = 0;
		float dz1 = 0;
		float dx2 = 0;
		float dz2 = 0;
		
		RANDOM.setSeed(entity.entityId);
		
		for (int i = 0; i < sectionCount; i++) {
			y2 = y + secDelta;
			
			tessellator.vertex(x1 + dx1,  y, z1 + dz1, 0F, 0F);
			tessellator.vertex(x1 + dx2, y2, z1 + dz2, 0F, 1F);
			tessellator.vertex(x2 + dx2, y2, z2 + dz2, 1F, 1F);
			tessellator.vertex(x2 + dx1,  y, z2 + dz1, 1F, 0F);
			
			if (i > 0 && RANDOM.nextInt(3) == 0) {
				float dist = RANDOM.nextFloat() * 15;
				float dx3 = dx * dist;
				float dz3 = dz * dist;
				
				tessellator.vertex(x1_2 + dx3,  y, z1_2 + dz3, 0F, 0F);
				tessellator.vertex(x1_2 + dx2, y2, z1_2 + dz2, 0F, 1F);
				tessellator.vertex(x2_2 + dx2, y2, z2_2 + dz2, 1F, 1F);
				tessellator.vertex(x2_2 + dx3,  y, z2_2 + dz3, 1F, 0F);
				
				if (RANDOM.nextBoolean()) {
					dist = RANDOM.nextFloat() * 15;
					float dx4 = dx * dist;
					float dz4 = dz * dist;
					float y3 = y - secDelta;
					
					tessellator.vertex(x1_2 + dx4, y3, z1_2 + dz4, 0F, 0F);
					tessellator.vertex(x1_2 + dx3,  y, z1_2 + dz3, 0F, 1F);
					tessellator.vertex(x2_2 + dx3,  y, z2_2 + dz3, 1F, 1F);
					tessellator.vertex(x2_2 + dx4, y3, z2_2 + dz4, 1F, 0F);
				}
			}
			
			dx1 = dx2;
			dz1 = dz2;
			float dist = RANDOM.nextFloat() * 7;
			dx2 = dx * dist;
			dz2 = dz * dist;
			
			y = y2;
		}
		
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
