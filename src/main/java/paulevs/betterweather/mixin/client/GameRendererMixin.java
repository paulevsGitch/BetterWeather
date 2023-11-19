package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.rendering.BetterWeatherRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow private int ambientSoundTick;
	@Shadow private Minecraft minecraft;
	@Shadow private float fogDistance;
	
	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderWeather(float delta, CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "weatherEffects", at = @At("HEAD"))
	private void betterweather_resetSounds(CallbackInfo info) {
		this.ambientSoundTick = -10;
	}
	
	@Inject(method = "setupFog", at = @At(
		value = "INVOKE",
		target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V",
		ordinal = 4,
		shift = Shift.AFTER,
		remap = false
	))
	private void betterweather_changeFogDepth(int f, float par2, CallbackInfo ci) {
		BetterWeatherRenderer.updateFogDepth(fogDistance);
	}
	
	@Inject(method = "delta", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/particle/ParticleManager;renderAll(Lnet/minecraft/entity/Entity;F)V",
		shift = Shift.AFTER
	))
	private void betterweather_renderBeforeWater(float delta, long time, CallbackInfo info) {
		BetterWeatherRenderer.renderBeforeWater(delta, minecraft);
	}
}
