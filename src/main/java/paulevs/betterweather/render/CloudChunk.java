package paulevs.betterweather.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class CloudChunk {
	private static final float[] RAIN_COLOR = new float[] { 66F / 255F, 74F / 255F, 74F / 255F };
	private static final float[] DARK_COLOR = new float[] { 150F / 255F, 176F / 255F, 211F / 255F };
	private static final Random RANDOM = new Random(0);
	
	private boolean needUpdate = true;
	private final int listID;
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
	
	public void update(int chunkX, int chunkZ, byte[] data) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glNewList(listID, GL11.GL_COMPILE);
		tessellator.start();
		
		for (short i = 0; i < 4096; i++) {
			if (data[i] == -1) continue;
			
			byte x = (byte) (i & 15);
			byte y = (byte) ((i >> 4) & 15);
			byte z = (byte) (i >> 8);
			
			boolean canDraw = x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15;
			if (!canDraw) {
				canDraw = data[i + 1] == -1 || data[i - 1] == -1 ||
					data[i + 16] == -1 || data[i - 16] == -1 ||
					data[i + 256] == -1 || data[i - 256] == -1;
			}
			
			if (!canDraw) continue;
			
			//tessellator.color((i & 15) * 17, ((i >> 4) & 15) * 17, (i >> 8) * 17);
			float delta = data[i] / 15F;
			tessellator.color(
				MathHelper.lerp(delta, DARK_COLOR[0], 1F),
				MathHelper.lerp(delta, DARK_COLOR[1], 1F),
				MathHelper.lerp(delta, DARK_COLOR[2], 1F)
			);
			makeCloudBlock(tessellator, x, y, z);
		}
		
		tessellator.draw();
		GL11.glEndList();
	}
	
	public void render(double entityX, double entityZ, float height) {
		float dx = (float) (posX - entityX);
		float dz = (float) (posZ - entityZ);
		GL11.glPushMatrix();
		GL11.glTranslatef(dx, height, dz);
		GL11.glScalef(2, 2, 2);
		GL11.glCallList(listID);
		GL11.glPopMatrix();
	}
	
	private void makeCloudBlock(Tessellator tessellator, int x, int y, int z) {
		RANDOM.setSeed(MathHelper.hashCode(x, y, z));
		
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
}
