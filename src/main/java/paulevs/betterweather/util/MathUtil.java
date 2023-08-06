package paulevs.betterweather.util;

public class MathUtil {
	public static int wrap(int value, int side) {
		if (net.modificationstation.stationapi.api.util.math.MathHelper.isPowerOfTwo(side)) {
			return value & (side - 1);
		}
		int result = (value - value / side * side);
		return result < 0 ? result + side : result;
	}
}
