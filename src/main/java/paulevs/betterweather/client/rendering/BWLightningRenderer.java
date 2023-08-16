package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.config.CommonConfig;

@Environment(EnvType.CLIENT)
public class BWLightningRenderer {
	private static int texture = -1;
	
	public static void render(LightningEntity entity, float x, float y, float z, TextureManager manager, float delta) {
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
		
		double x1 = x + 0.5F + dx;
		double x2 = x + 0.5F - dx;
		double z1 = z + 0.5F + dz;
		double z2 = z + 0.5F - dz;
		
		float u1 = MathHelper.floor(((double) entity.level.getLevelTime() + delta) * 0.5 % 6.0) / 6F;
		float u2 = u1 + 1 / 6F;
		float v1 = (float) entity.y + MathHelper.floor(((double) entity.level.getLevelTime() + delta) * 0.2 % 6.0) / 6F;
		float v2 = v1 + (y2 - y) / 8F;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		tessellator.start();
		tessellator.color(255, 255, 255, 255);
		tessellator.vertex(x1,  y, z1, u1, v1);
		tessellator.vertex(x1, y2, z1, u1, v2);
		tessellator.vertex(x2, y2, z2, u2, v2);
		tessellator.vertex(x2,  y, z2, u2, v1);
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		//GL11.glDisable(GL11.GL_BLEND);
		
		//tessellator.setOffset(0, 0, 0);
	}
}
