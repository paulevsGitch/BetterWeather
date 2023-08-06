package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.maths.Vec3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CloudTexture {
	private final IntBuffer pixels;
	private final Random random = new Random();
	private final int textureID;
	private final int capacity;
	private final int width;
	private final int height;
	
	public CloudTexture(TextureManager manager) {
		textureID = manager.getTextureId("/assets/better_weather/textures/cloud.png");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		capacity = width * height;
		pixels = ByteBuffer.allocateDirect(capacity << 2).asIntBuffer();
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		pixels.position(0);
	}
	
	public void bindAndUpdate(Vec3f color) {
		update(color);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, pixels);
	}
	
	private void update(Vec3f color) {
		int rgb = (int) (color.x * 255) << 24 | (int) (color.y * 255) << 16 | (int) (color.z * 255) << 8;
		for (int i = 0; i < capacity; i++) {
			int alpha = pixels.get(i) & 255;
			pixels.put(i, rgb | alpha);
		}
		pixels.position(0);
	}
}
