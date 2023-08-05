package paulevs.betterweather.render;

import net.minecraft.util.maths.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageSampler {
	private final float[] data;
	private final int width;
	private final int height;
	
	public ImageSampler(String path) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);
		BufferedImage image;
		
		try {
			image = ImageIO.read(url);
		}
		catch (IOException e) {
			e.printStackTrace();
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		
		width = image.getWidth();
		height = image.getWidth();
		data = new float[width * height];
		
		int[] pixels = new int[data.length];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		
		for (int i = 0; i < data.length; i++) {
			data[i] = (pixels[i] & 255) / 255F;
		}
	}
	
	public float sample(double x, double z) {
		int x1 = MathHelper.floor(x);
		int z1 = MathHelper.floor(z);
		int x2 = MathUtil.wrap(x1 + 1, width);
		int z2 = MathUtil.wrap(z1 + 1, height);
		float dx = (float) (x - x1);
		float dz = (float) (z - z1);
		x1 = MathUtil.wrap(x1, width);
		z1 = MathUtil.wrap(z1, height);
		
		float a = data[getIndex(x1, z1)];
		float b = data[getIndex(x2, z1)];
		float c = data[getIndex(x1, z2)];
		float d = data[getIndex(x2, z2)];
		
		return net.modificationstation.stationapi.api.util.math.MathHelper.interpolate2D(
			dx, dz, a, b, c, d
		);
	}
	
	private int getIndex(int x, int z) {
		return z * width + x;
	}
}
