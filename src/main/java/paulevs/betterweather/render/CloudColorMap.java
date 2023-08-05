package paulevs.betterweather.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class CloudColorMap {
	private final int[] pixels = new int[256];
	private final int textureID;
	
	public CloudColorMap() {
		this.textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		//GL11.glTexSubImage2D(3553, 0, 0, 0, i, j, 6408, 5121, this.currentImageBuffer);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void render() {
	
	}
}
