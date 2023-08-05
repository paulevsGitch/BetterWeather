package paulevs.betterweather.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.maths.Vec2i;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CloudRenderer {
	private static final ImageSampler MAIN_SHAPE_SAMPLER = new ImageSampler("assets/better_weather/textures/main_shape.png");
	private static final ImageSampler LARGE_DETAILS_SAMPLER = new ImageSampler("assets/better_weather/textures/large_details.png");
	private static final ImageSampler VARIATION_SAMPLER = new ImageSampler("assets/better_weather/textures/variation.png");
	private static final byte[] CLOUD_DATA = new byte[4096];
	private static final Random RANDOM = new Random(0);
	
	private static final int RADIUS = 9;
	private static final int SIDE = RADIUS * 2 + 1;
	private static final int CAPACITY = SIDE * SIDE;
	private static final double SPEED = 0.001; // Chunks per tick
	
	private final CloudChunk[] chunks = new CloudChunk[CAPACITY];
	private final float[] cloudShape = new float[16];
	private final Vec2i[] offsets;
	
	//private int cloudTexture = 0;
	private CloudTexture cloudTexture;
	
	public CloudRenderer() {
		for (short i = 0; i < chunks.length; i++) {
			chunks[i] = new CloudChunk();
		}
		
		List<Vec2i> offsets = new ArrayList<>(CAPACITY);
		for (byte x = -RADIUS; x <= RADIUS; x++) {
			for (byte z = -RADIUS; z <= RADIUS; z++) {
				offsets.add(new Vec2i(x, z));
			}
		}
		offsets.sort((v1, v2) -> {
			int d1 = v1.x * v1.x + v1.z * v1.z;
			int d2 = v2.x * v2.x + v2.z * v2.z;
			return Integer.compare(d1, d2);
		});
		this.offsets = offsets.toArray(Vec2i[]::new);
		
		for (byte i = 0; i < 4; i++) {
			cloudShape[i] = (4 - i) / 4F;
			cloudShape[i] *= cloudShape[i];
		}
		for (byte i = 4; i < 16; i++) {
			cloudShape[i] = (i - 4) / 12F;
			cloudShape[i] *= cloudShape[i];
		}
	}
	
	public void update(TextureManager manager) {
		/*if (cloudTexture == 0) {
			cloudTexture = manager.getTextureId("/assets/better_weather/textures/cloud.png");
		}*/
		if (cloudTexture == null) {
			cloudTexture = new CloudTexture(manager);
		}
	}
	
	private int getIndex(int x, int y) {
		return MathUtil.wrap(x, SIDE) * SIDE + MathUtil.wrap(y, SIDE);
	}
	
	public void render(Minecraft minecraft, float delta) {
		double entityX = MathHelper.lerp(delta, minecraft.viewEntity.prevRenderX, minecraft.viewEntity.x);
		double entityY = MathHelper.lerp(delta, minecraft.viewEntity.prevRenderY, minecraft.viewEntity.y);
		double entityZ = MathHelper.lerp(delta, minecraft.viewEntity.prevRenderZ, minecraft.viewEntity.z);
		float height = (float) (minecraft.level.dimension.getCloudHeight() - entityY);
		
		int centerX = net.minecraft.util.maths.MathHelper.floor(entityX / 32);
		int centerZ = net.minecraft.util.maths.MathHelper.floor(entityZ / 32);
		
		double moveDelta = ((double) minecraft.level.getLevelTime() + delta) * SPEED;
		int worldOffset = (int) Math.floor(moveDelta);
		entityZ -= (moveDelta - worldOffset) * 32;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, cloudTexture);
		
		cloudTexture.bindAndUpdate(minecraft.level.getSunPosition(delta));
		
		boolean canUpdate = true;
		
		for (Vec2i offset : offsets) {
			int cx = centerX + offset.x;
			int cz = centerZ + offset.z;
			int movedZ = cz - worldOffset;
			CloudChunk chunk = chunks[getIndex(cx, movedZ)];
			chunk.setRenderPosition(cx, cz);
			chunk.checkIfNeedUpdate(cx, movedZ);
			if (canUpdate && chunk.needUpdate()) {
				updateData(cx, movedZ);
				chunk.update(cx, movedZ, CLOUD_DATA);
				canUpdate = false;
			}
			if (!chunk.needUpdate()) {
				chunk.render(entityX, entityZ, height);
			}
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	private float getDensity(int x, int y, int z) {
		float density = MAIN_SHAPE_SAMPLER.sample(x * 1.5F, z * 1.5F);
		density += LARGE_DETAILS_SAMPLER.sample(x * 5, z * 5);
		
		density -= VARIATION_SAMPLER.sample(y * 5, x * 5) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 5, y * 5) * 0.05F;
		density -= VARIATION_SAMPLER.sample(z * 5, x * 5) * 0.05F;
		
		int value = (int) (MathHelper.hashCode(x, y, z) % 3);
		density -= value * 0.01F;
		
		density -= cloudShape[y];
		return density;
	}
	
	private void updateData(int cx, int cz) {
		cx <<= 4;
		cz <<= 4;
		
		float coverage = 1.2F;
		
		for (short index = 0; index < 4096; index++) {
			int x = index & 15;
			int y = (index >> 4) & 15;
			int z = index >> 8;
			
			x |= cx;
			z |= cz;
			
			float density = getDensity(x, y, z);
			
			if (density < coverage) {
				CLOUD_DATA[index] = -1;
				continue;
			}
			
			byte light = 15;
			for (byte i = (byte) (y + 1); i < 15; i++) {
				if (getDensity(x, i, z) >= coverage) light--;
			}
			
			RANDOM.setSeed(MathHelper.hashCode(x, y, z));
			light = (byte) MathHelper.clamp(light + RANDOM.nextInt(3) - 1, 0, 15);
			
			CLOUD_DATA[index] = light;
		}
	}
}
