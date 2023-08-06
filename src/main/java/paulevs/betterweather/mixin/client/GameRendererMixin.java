package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.api.WeatherAPI;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow private int ambientSoundTick;
	@Shadow private Minecraft minecraft;
	@Shadow private float fogDistance;
	@Shadow float fogColorR;
	@Shadow float fogColorG;
	@Shadow float fogColorB;
	
	@Unique private float betterweather_raining;
	
	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderWeather(float delta, CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "weatherEffects", at = @At("HEAD"))
	private void betterweather_resetSounds(CallbackInfo info) {
		this.ambientSoundTick = -10;
	}
	
	@Inject(method = "renderFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/level/Level;getRainGradient(F)F"))
	private void betterweather_renderFog(float delta, CallbackInfo info) {
		betterweather_raining = WeatherAPI.getRainDensity(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z
		);
		if (betterweather_raining == 0) return;
		float power = 1F - betterweather_raining * 0.75F;
		this.fogColorR *= power;
		this.fogColorG *= power;
		this.fogColorB *= power;
	}
	
	@Inject(method = "setupFog", at = @At(
		value = "INVOKE",
		target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V",
		ordinal = 4,
		shift = Shift.AFTER
	))
	private void betterweather_changeFogDepth(int f, float par2, CallbackInfo ci) {
		if (betterweather_raining == 0) return;
		float power = 1F - betterweather_raining * 0.75F;
		GL11.glFogf(GL11.GL_FOG_START, this.fogDistance * power * 0.25F);
		GL11.glFogf(GL11.GL_FOG_END, this.fogDistance * power);
	}
}
