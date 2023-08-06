package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.maths.Vec3f;
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
	
	@Unique private float betterweather_fogDistance = 1F;
	
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
		betterweather_fogDistance = 1F;
		
		betterweather_fogDistance = WeatherAPI.getRainDensity(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z
		);
		betterweather_fogDistance = 1F - betterweather_fogDistance * 0.75F;
		
		this.fogColorR *= betterweather_fogDistance;
		this.fogColorG *= betterweather_fogDistance;
		this.fogColorB *= betterweather_fogDistance;
		
		float inCloud = WeatherAPI.inCloud(
			minecraft.level,
			minecraft.viewEntity.x,
			minecraft.viewEntity.y,
			minecraft.viewEntity.z
		);
		
		if (inCloud > 0) {
			Vec3f fogColor = minecraft.level.getSunPosition(delta);
			betterweather_fogDistance = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(
				inCloud, betterweather_fogDistance, 0.02F
			);
			this.fogColorR = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(
				inCloud, this.fogColorR, (float) fogColor.x
			);
			this.fogColorG = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(
				inCloud, this.fogColorG, (float) fogColor.y
			);
			this.fogColorB = net.modificationstation.stationapi.api.util.math.MathHelper.lerp(
				inCloud, this.fogColorB, (float) fogColor.z
			);
		}
	}
	
	@Inject(method = "setupFog", at = @At(
		value = "INVOKE",
		target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V",
		ordinal = 4,
		shift = Shift.AFTER
	))
	private void betterweather_changeFogDepth(int f, float par2, CallbackInfo ci) {
		if (betterweather_fogDistance == 1) return;
		GL11.glFogf(GL11.GL_FOG_START, this.fogDistance * betterweather_fogDistance * 0.25F);
		GL11.glFogf(GL11.GL_FOG_END, this.fogDistance * betterweather_fogDistance);
	}
}
