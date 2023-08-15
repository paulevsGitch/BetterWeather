package paulevs.betterweather.mixin.client;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.rendering.BetterWeatherRenderer;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Inject(method = "changeOption", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/LevelRenderer;updateFromOptions()V",
		ordinal = 1
	))
	private void betterweather_changeOption(Option option, int value, CallbackInfo info) {
		BetterWeatherRenderer.updateClouds();
	}
}
