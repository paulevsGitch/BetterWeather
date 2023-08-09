package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.maths.MathHelper;
import net.modificationstation.stationapi.api.util.math.Quaternion;
import net.modificationstation.stationapi.api.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class FrustumCulling {
	private static final float TO_RADIANS = (float) (Math.PI / 180);
	private static final Vec3f[] NORMALS;
	private final Quaternion rotation = new Quaternion(0, 0, 0, 0);
	private final Quaternion rotation2 = new Quaternion(0, 0, 0, 0);
	private final Vec3f[] defaultNormals;
	private final Vec3f[] planes;
	
	public FrustumCulling() {
		defaultNormals = new Vec3f[4];
		planes = new Vec3f[4];
		for (byte i = 0; i < 4; i++) {
			defaultNormals[i] = NORMALS[i].copy();
			planes[i] = NORMALS[i].copy();
		}
	}
	
	public void setFOV(float angle) {
		for (byte i = 0; i < 4; i++) {
			Vec3f original = NORMALS[i];
			Vec3f normal = defaultNormals[i];
			normal.set(original.getX(), original.getY(), original.getZ());
			if (normal.getX() != 0) setRotation(rotation, Vec3f.POSITIVE_Y, normal.getX() > 0 ? angle : -angle);
			else setRotation(rotation, Vec3f.POSITIVE_X, normal.getY() > 0 ? -angle : angle);
			normal.rotate(rotation);
		}
	}
	
	public void rotate(float yaw, float pitch) {
		setRotation(rotation2, Vec3f.POSITIVE_X, pitch * TO_RADIANS);
		setRotation(rotation, Vec3f.POSITIVE_Y, -yaw * TO_RADIANS);
		rotation.hamiltonProduct(rotation2);
		for (byte i = 0; i < 4; i++) {
			Vec3f normal = defaultNormals[i];
			planes[i].set(normal.getX(), normal.getY(), normal.getZ());
			planes[i].rotate(rotation);
		}
	}
	
	public boolean isOutside(Vec3f pos, float distance) {
		for (byte i = 0; i < 4; i++) {
			if (planes[i].dot(pos) > distance) return true;
		}
		return false;
	}
	
	private void setRotation(Quaternion rotation, Vec3f axis, float angle) {
		angle *= 0.5F;
		float sin = MathHelper.sin(angle);
		rotation.set(
			axis.getX() * sin,
			axis.getY() * sin,
			axis.getZ() * sin,
			MathHelper.cos(angle)
		);
	}
	
	static {
		NORMALS = new Vec3f[] {
			new Vec3f( 1, 0, 0),
			new Vec3f(-1, 0, 0),
			new Vec3f(0,  1, 0),
			new Vec3f(0, -1, 0)
		};
	}
}