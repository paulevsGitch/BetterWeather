package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import net.modificationstation.stationapi.api.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import paulevs.betterweather.config.ClientConfig;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class CloudChunk {
	private static final float[] RAIN_COLOR = new float[] { 66F / 255F, 74F / 255F, 74F / 255F };
	private static final float[] DARK_COLOR = new float[] { 150F / 255F, 176F / 255F, 211F / 255F };
	private static final Random RANDOM = new Random(0);
	private static final Vec3f POS = new Vec3f();
	
	private final int listID;
	
	private boolean needUpdate = true;
	private boolean isEmpty = true;
	private int chunkX = Integer.MIN_VALUE;
	private int chunkZ = Integer.MIN_VALUE;
	private int posX;
	private int posZ;
	
	public CloudChunk() {
		listID = GL11.glGenLists(1);
	}
	
	public void checkIfNeedUpdate(int x, int z) {
		needUpdate = chunkX != x || chunkZ != z;
	}
	
	public void forceUpdate() {
		chunkX = Integer.MIN_VALUE;
		chunkZ = Integer.MIN_VALUE;
		isEmpty = true;
	}
	
	public boolean needUpdate() {
		return needUpdate;
	}
	
	public void setRenderPosition(int chunkX, int chunkZ) {
		this.posX = chunkX << 5;
		this.posZ = chunkZ << 5;
	}
	
	public void update(int chunkX, int chunkZ, short[] data) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		isEmpty = true;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glNewList(listID, GL11.GL_COMPILE);
		tessellator.start();
		
		for (short i = 0; i < 8192; i++) {
			if (data[i] == CloudRenderer.EMPTY_CLOUD) continue;
			
			byte x = (byte) (i & 15);
			byte y = (byte) ((i >> 4) & 31);
			byte z = (byte) (i >> 9);
			
			boolean canDraw = x == 0 || x == 15 || y == 0 || y == 31 || z == 0 || z == 15;
			if (!canDraw) {
				canDraw = data[i + 1] == CloudRenderer.EMPTY_CLOUD || data[i - 1] == CloudRenderer.EMPTY_CLOUD ||
					data[i + 16] == CloudRenderer.EMPTY_CLOUD || data[i - 16] == CloudRenderer.EMPTY_CLOUD ||
					data[i + 512] == CloudRenderer.EMPTY_CLOUD || data[i - 512] == CloudRenderer.EMPTY_CLOUD;
			}
			
			if (!canDraw) continue;
			
			RANDOM.setSeed(MathHelper.hashCode(x, y, z));
			float deltaBrightness = ((data[i] & 15) + RANDOM.nextFloat()) / 15F;
			float deltaWetness = (((data[i] >> 4) & 15) + RANDOM.nextFloat()) / 15F;
			float deltaThunder = ((data[i] >> 8) & 15) / 15F;
			deltaBrightness *= (1 - deltaWetness) * 0.5F + 0.5F;
			deltaThunder = MathHelper.lerp(deltaThunder, 1F, 0.5F);
			
			float r = MathHelper.lerp(deltaWetness, RAIN_COLOR[0], DARK_COLOR[0]);
			float g = MathHelper.lerp(deltaWetness, RAIN_COLOR[1], DARK_COLOR[1]);
			float b = MathHelper.lerp(deltaWetness, RAIN_COLOR[2], DARK_COLOR[2]);
			r = MathHelper.lerp(deltaBrightness, r, 1F) * deltaThunder;
			g = MathHelper.lerp(deltaBrightness, g, 1F) * deltaThunder;
			b = MathHelper.lerp(deltaBrightness, b, 1F) * deltaThunder;
			
			tessellator.color(r, g, b);
			if (ClientConfig.renderFluffy()) {
				makeFluffyCloudBlock(tessellator, x, y, z);
			}
			else {
				makeNormalCloudBlock(tessellator, x, y, z, data, i);
			}
			isEmpty = false;
		}
		
		tessellator.draw();
		GL11.glEndList();
	}
	
	public void render(double entityX, double entityZ, float height, FrustumCulling culling, float distanceSqr) {
		if (isEmpty) return;
		float dx = (float) (posX - entityX);
		float dz = (float) (posZ - entityZ);
		POS.set(dx + 16, height + 16, dz + 16);
		if (POS.getX() * POS.getX() + POS.getZ() * POS.getZ() > distanceSqr) return;
		if (culling.isOutside(POS, 24)) return;
		GL11.glPushMatrix();
		GL11.glTranslatef(dx, height, dz);
		GL11.glScalef(2, 2, 2);
		GL11.glCallList(listID);
		GL11.glPopMatrix();
	}
	
	private void makeFluffyCloudBlock(Tessellator tessellator, int x, int y, int z) {
		float px = x + RANDOM.nextFloat() * 0.1F - 0.05F;
		float py = y + RANDOM.nextFloat() * 0.1F - 0.05F;
		float pz = z + RANDOM.nextFloat() * 0.1F - 0.05F;
		
		tessellator.vertex(px - 0.207107F, py + 1.207107F, pz + 0.5F, 0.0F, 0.0F);
		tessellator.vertex(px + 1.207107F, py - 0.207107F, pz + 0.5F, 1.0F, 0.0F);
		tessellator.vertex(px + 1.207107F, py - 0.207107F, pz - 1.5F, 1.0F, 1.0F);
		tessellator.vertex(px - 0.207107F, py + 1.207107F, pz - 1.5F, 0.0F, 1.0F);
		
		tessellator.vertex(px + 1.207107F, py + 1.207107F, pz + 0.5F, 0.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py - 0.207107F, pz + 0.5F, 1.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py - 0.207107F, pz - 1.5F, 1.0F, 1.0F);
		tessellator.vertex(px + 1.207107F, py + 1.207107F, pz - 1.5F, 0.0F, 1.0F);
		
		tessellator.vertex(px + 1.5F, py + 1.207107F, pz + 0.207107F, 0.0F, 0.0F);
		tessellator.vertex(px + 1.5F, py - 0.207107F, pz - 1.207107F, 1.0F, 0.0F);
		tessellator.vertex(px - 0.5F, py - 0.207107F, pz - 1.207107F, 1.0F, 1.0F);
		tessellator.vertex(px - 0.5F, py + 1.207107F, pz + 0.207107F, 0.0F, 1.0F);
		
		tessellator.vertex(px + 1.5F, py + 1.207107F, pz - 1.207107, 0.0F, 0.0F);
		tessellator.vertex(px + 1.5F, py - 0.207107F, pz + 0.207107, 1.0F, 0.0F);
		tessellator.vertex(px - 0.5F, py - 0.207107F, pz + 0.207107, 1.0F, 1.0F);
		tessellator.vertex(px - 0.5F, py + 1.207107F, pz - 1.207107, 0.0F, 1.0F);
		
		tessellator.vertex(px + 1.207107F, py - 0.5F, pz + 0.207107, 0.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py - 0.5F, pz - 1.207107, 1.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py + 1.5F, pz - 1.207107, 1.0F, 1.0F);
		tessellator.vertex(px + 1.207107F, py + 1.5F, pz + 0.207107, 0.0F, 1.0F);
		
		tessellator.vertex(px + 1.207107F, py - 0.5F, pz - 1.207107F, 0.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py - 0.5F, pz + 0.207107F, 1.0F, 0.0F);
		tessellator.vertex(px - 0.207107F, py + 1.5F, pz + 0.207107F, 1.0F, 1.0F);
		tessellator.vertex(px + 1.207107F, py + 1.5F, pz - 1.207107F, 0.0F, 1.0F);
	}
	
	private void makeNormalCloudBlock(Tessellator tessellator, int x, int y, int z, short[] data, int index) {
		if (x == 0 || data[index - 1] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x, y, z, 0.5F, 0.5F);
			tessellator.vertex(x, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x, y + 1, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x, y, z + 1, 0.5F, 0.5F);
		}
		if (x == 15 || data[index + 1] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x + 1, y, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y, z + 1, 0.5F, 0.5F);
		}
		
		if (y == 0 || data[index - 16] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x, y, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x, y, z + 1, 0.5F, 0.5F);
		}
		if (y == 31 || data[index + 16] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x, y + 1, z + 1, 0.5F, 0.5F);
		}
		
		if (z == 0 || data[index - 512] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x, y, z, 0.5F, 0.5F);
			tessellator.vertex(x, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y, z, 0.5F, 0.5F);
		}
		if (z == 15 || data[index + 512] == CloudRenderer.EMPTY_CLOUD) {
			tessellator.vertex(x, y, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x, y + 1, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y + 1, z + 1, 0.5F, 0.5F);
			tessellator.vertex(x + 1, y, z + 1, 0.5F, 0.5F);
		}
	}
}
