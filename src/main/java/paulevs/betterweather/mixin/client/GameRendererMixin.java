package paulevs.betterweather.mixin.client;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderWeather(float delta, CallbackInfo info) {
		info.cancel();
	}
}
