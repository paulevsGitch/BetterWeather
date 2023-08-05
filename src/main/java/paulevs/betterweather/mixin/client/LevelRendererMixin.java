package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.render.CloudRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow private Minecraft minecraft;
	
	@Unique private CloudRenderer weather_cloudArea = new CloudRenderer();
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterweather_onInit(Minecraft minecraft, TextureManager manager, CallbackInfo info) {
		weather_cloudArea.update(manager);
	}
	
	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderClouds(float delta, CallbackInfo info) {
		weather_cloudArea.render(this.minecraft, delta);
		info.cancel();
	}
}
