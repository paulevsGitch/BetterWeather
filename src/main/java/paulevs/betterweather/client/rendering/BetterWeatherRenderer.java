package paulevs.betterweather.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.maths.Vec3D;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import paulevs.betterweather.api.WeatherAPI;

@Environment(EnvType.CLIENT)
public class BetterWeatherRenderer {
	private static final CloudRenderer CLOUD_RENDERER = new CloudRenderer();
	private static final WeatherRenderer WEATHER_RENDERER = new WeatherRenderer();
	private static float fogDistance = 1F;
	private static boolean isInWater;
	public static float fogColorR;
	public static float fogColorG;
	public static float fogColorB;
	
	public static void update(TextureManager manager) {
		CLOUD_RENDERER.update(manager);
		WEATHER_RENDERER.update(manager);
	}
	
	public static void renderBeforeWater(float delta, Minecraft minecraft) {
		if (minecraft.level.dimension.evaporatesWater) return;
		isInWater = minecraft.viewEntity.isInFluid(Material.WATER);
		if (!isInWater) return;
		render(delta, minecraft);
	}
	
	public static void renderAfterWater(float delta, Minecraft minecraft) {
		if (minecraft.level.dimension.evaporatesWater) return;
		if (isInWater) return;
		render(delta, minecraft);
	}
	
	private static void render(float delta, Minecraft minecraft) {
		CLOUD_RENDERER.render(delta, minecraft);
		WEATHER_RENDERER.render(delta, minecraft);
	}
	
	public static void changeFogDensity(Args args, Minecraft minecraft) {
		float density = WeatherAPI.getRainDensity(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z,
			true
		);
		if (density == 0) return;
		density = 1F - density * 0.75F;
		args.set(0, (float) args.get(0) * density);
		args.set(1, (float) args.get(1) * density);
		args.set(2, (float) args.get(2) * density);
	}
	
	public static void updateFogColor(Minecraft minecraft, float delta) {
		fogDistance = 1F;
		
		fogDistance = WeatherAPI.getRainDensity(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z,
			true
		);
		fogDistance = 1F - fogDistance * 0.75F;
		
		fogColorR *= fogDistance;
		fogColorG *= fogDistance;
		fogColorB *= fogDistance;
		
		float inCloud = WeatherAPI.inCloud(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z
		);
		
		if (inCloud > 0) {
			Vec3D fogColor = minecraft.level.getSunPosition(delta);
			fogDistance = MathHelper.lerp(inCloud, fogDistance, 0.02F);
			fogColorR = MathHelper.lerp(inCloud, fogColorR, (float) fogColor.x);
			fogColorG = MathHelper.lerp(inCloud, fogColorG, (float) fogColor.y);
			fogColorB = MathHelper.lerp(inCloud, fogColorB, (float) fogColor.z);
		}
	}
	
	public static void updateFogDepth(float defaultFogDistance) {
		CloudRenderer.fogDistance = defaultFogDistance;
		if (fogDistance == 1) return;
		GL11.glFogf(GL11.GL_FOG_START, defaultFogDistance * fogDistance * 0.25F);
		GL11.glFogf(GL11.GL_FOG_END, defaultFogDistance * fogDistance);
	}
	
	public static void updateClouds() {
		CLOUD_RENDERER.updateAll();
	}
}
