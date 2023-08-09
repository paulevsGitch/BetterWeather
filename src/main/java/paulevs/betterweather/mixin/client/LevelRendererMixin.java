package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.client.rendering.CloudRenderer;
import paulevs.betterweather.client.rendering.WeatherRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow private Minecraft minecraft;
	@Shadow private int glEndList;
	@Unique private final CloudRenderer weather_cloudArea = new CloudRenderer();
	@Unique private final WeatherRenderer betterweather_weatherRenderer = new WeatherRenderer();
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterweather_onInit(Minecraft minecraft, TextureManager manager, CallbackInfo info) {
		weather_cloudArea.update(manager);
		betterweather_weatherRenderer.update(manager);
		
		GL11.glNewList(this.glEndList, GL11.GL_COMPILE);
		GL11.glEndList();
	}
	
	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderClouds(float delta, CallbackInfo info) {
		weather_cloudArea.render(minecraft, delta);
		betterweather_weatherRenderer.render(delta, minecraft.level, minecraft.viewEntity, minecraft.options.fancyGraphics);
		info.cancel();
	}
	
	@ModifyArgs(method = "renderSky", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V"))
	private void injected(Args args) {
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
}
