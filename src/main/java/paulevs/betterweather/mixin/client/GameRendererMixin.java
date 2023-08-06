package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.render.WeatherRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow private Minecraft minecraft;
	
	@Unique private WeatherRenderer betterweather_weatherRenderer;
	
	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderWeather(float delta, CallbackInfo info) {
		if (betterweather_weatherRenderer == null) {
			betterweather_weatherRenderer = new WeatherRenderer(minecraft.textureManager);
		}
		betterweather_weatherRenderer.render(delta, minecraft.level, minecraft.viewEntity, minecraft.options.fancyGraphics);
		info.cancel();
	}
}
