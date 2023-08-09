package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import net.modificationstation.stationapi.api.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

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
			if (data[i] == -1) continue;
			
			byte x = (byte) (i & 15);
			byte y = (byte) ((i >> 4) & 31);
			byte z = (byte) (i >> 9);
			
			boolean canDraw = x == 0 || x == 15 || y == 0 || y == 31 || z == 0 || z == 15;
			if (!canDraw) {
				canDraw = data[i + 1] == -1 || data[i - 1] == -1 ||
					data[i + 32] == -1 || data[i - 32] == -1 ||
					data[i + 512] == -1 || data[i - 512] == -1;
			}
			
			if (!canDraw) continue;
			
			RANDOM.setSeed(MathHelper.hashCode(x, y, z));
			float deltaBrightness = ((data[i] & 15) + RANDOM.nextFloat()) / 16F;
			float deltaWetness = (((data[i] >> 4) & 15) + RANDOM.nextFloat()) / 16F;
			deltaBrightness *= (1 - deltaWetness) * 0.5F + 0.5F;
			
			float r = MathHelper.lerp(deltaWetness, RAIN_COLOR[0], DARK_COLOR[0]);
			float g = MathHelper.lerp(deltaWetness, RAIN_COLOR[1], DARK_COLOR[1]);
			float b = MathHelper.lerp(deltaWetness, RAIN_COLOR[2], DARK_COLOR[2]);
			r = MathHelper.lerp(deltaBrightness, r, 1F);
			g = MathHelper.lerp(deltaBrightness, g, 1F);
			b = MathHelper.lerp(deltaBrightness, b, 1F);
			
			tessellator.color(r, g, b);
			makeCloudBlock(tessellator, x, y, z);
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
	
	private void makeCloudBlock(Tessellator tessellator, int x, int y, int z) {
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
	
	private boolean pointIsVisible(double nx, double ny, double nz, double x, double y, double z) {
		return nx * x + ny * y + nz * z > 0;
	}
}
