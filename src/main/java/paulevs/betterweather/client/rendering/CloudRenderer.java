package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.maths.Vec2i;
import net.minecraft.util.noise.PerlinNoise;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.config.CommonConfig;
import paulevs.betterweather.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class CloudRenderer {
	private static final PerlinNoise NOISE = new PerlinNoise(new Random(0));
	private static final short[] CLOUD_DATA = new short[8192];
	
	private static final int RADIUS = 9;
	private static final int SIDE = RADIUS * 2 + 1;
	private static final int CAPACITY = SIDE * SIDE;
	public static float fogDistance;
	
	private final CloudChunk[] chunks = new CloudChunk[CAPACITY];
	private final FrustumCulling culling = new FrustumCulling();
	private final Vec2i[] offsets;
	
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
		culling.setFOV((float) Math.toRadians(60F));
	}
	
	public void update(TextureManager manager) {
		if (cloudTexture == null) {
			cloudTexture = new CloudTexture(manager);
		}
	}
	
	private int getIndex(int x, int y) {
		return MathUtil.wrap(x, SIDE) * SIDE + MathUtil.wrap(y, SIDE);
	}
	
	public void render(float delta, Minecraft minecraft) {
		LivingEntity entity = minecraft.viewEntity;
		double entityX = MathHelper.lerp(delta, entity.prevRenderX, entity.x);
		double entityY = MathHelper.lerp(delta, entity.prevRenderY, entity.y);
		double entityZ = MathHelper.lerp(delta, entity.prevRenderZ, entity.z);
		float height = (float) (minecraft.level.dimension.getCloudHeight() - entityY);
		
		int centerX = net.minecraft.util.maths.MathHelper.floor(entityX / 32);
		int centerZ = net.minecraft.util.maths.MathHelper.floor(entityZ / 32);
		
		double moveDelta = ((double) minecraft.level.getLevelTime() + delta) * CommonConfig.getCloudsSpeed();
		int worldOffset = (int) Math.floor(moveDelta);
		entityZ -= (moveDelta - worldOffset) * 32;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		cloudTexture.bindAndUpdate(minecraft.level.getSunPosition(delta));
		culling.rotate(entity.yaw, entity.pitch);
		
		boolean canUpdate = true;
		float distance = fogDistance * 1.5F;
		distance *= distance;
		
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
				chunk.render(entityX, entityZ, height, culling, distance);
			}
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
	}
	
	public void updateAll() {
		Arrays.stream(chunks).forEach(CloudChunk::forceUpdate);
	}
	
	private void updateData(int cx, int cz) {
		final int posX = cx << 4;
		final int posZ = cz << 4;
		
		IntStream.range(0, 8192).parallel().forEach(index -> {
			int x = index & 15;
			int y = (index >> 4) & 31;
			int z = index >> 9;
			
			x |= posX;
			z |= posZ;
			
			float rainFront = WeatherAPI.sampleFront(x, z, 0.2);
			float density = WeatherAPI.getCloudDensity(x << 1, y << 1, z << 1, rainFront);
			float coverage = WeatherAPI.getCoverage(rainFront);
			
			if (density < coverage) {
				CLOUD_DATA[index] = -1;
			}
			else {
				CLOUD_DATA[index] = (byte) ((byte) (rainFront * 15) << 4);
			}
		});
		
		IntStream.range(0, 8192).parallel().forEach(index -> {
			if (CLOUD_DATA[index] == -1) return;
			
			int x = index & 15;
			int y = (index >> 4) & 31;
			int z = index >> 9;
			
			x |= cx;
			z |= cz;
			
			byte light = 15;
			for (byte i = 1; i < 15; i++) {
				if (y + i > 31) break;
				int index2 = index + (i << 4);
				if (CLOUD_DATA[index2] != -1) light--;
			}
			
			if (light > 0) {
				light -= NOISE.sample(x * 0.3, y * 0.3, z * 0.3);
			}
			
			CLOUD_DATA[index] |= light;
		});
	}
}
