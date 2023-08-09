package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.modificationstation.stationapi.api.util.math.Quaternion;
import net.modificationstation.stationapi.api.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class FrustumCulling {
	private static final float TO_RADIANS = (float) (Math.PI / 180);
	private static final Vec3f[] NORMALS;
	private final Quaternion rotation = new Quaternion(0, 0, 0, 0);
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
			if (normal.getX() != 0) setRotation(Vec3f.POSITIVE_Y, normal.getX() > 0 ? angle : -angle);
			else setRotation(Vec3f.POSITIVE_X, normal.getY() > 0 ? -angle : angle);
			normal.rotate(rotation);
		}
	}
	
	public void rotate(float yaw, float pitch) {
		setRotation(-pitch * TO_RADIANS, -yaw * TO_RADIANS, 0);
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
	
	private void setRotation(Vec3f axis, float angle) {
		float sin = (float) Math.sin(angle / 2.0F);
		rotation.set(
			axis.getX() * sin,
			axis.getY() * sin,
			axis.getZ() * sin,
			(float) Math.cos(angle / 2.0F)
		);
	}
	
	private void setRotation(float x, float y, float z) {
		float f = (float) Math.sin(0.5F * x);
		float g = (float) Math.cos(0.5F * x);
		float h = (float) Math.sin(0.5F * y);
		float i = (float) Math.cos(0.5F * y);
		float j = (float) Math.sin(0.5F * z);
		float k = (float) Math.cos(0.5F * z);
		rotation.set(
			f * i * k + g * h * j,
			g * h * k - f * i * j,
			f * h * k + g * i * j,
			g * i * k - f * h * j
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