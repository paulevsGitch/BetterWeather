package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.impl.worldgen.FogRendererImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.rendering.BetterWeatherRenderer;

@Mixin(value = FogRendererImpl.class, remap = false)
public class FogRendererImplMixin {
	@Shadow @Final private static float[] FOG_COLOR;
	
	@Inject(method = "setupFog", at = @At("TAIL"))
	private static void betterweather_changeFogColor(Minecraft minecraft, float delta, CallbackInfo info) {
		BetterWeatherRenderer.fogColorR = FOG_COLOR[0];
		BetterWeatherRenderer.fogColorG = FOG_COLOR[1];
		BetterWeatherRenderer.fogColorB = FOG_COLOR[2];
		BetterWeatherRenderer.updateFogColor(minecraft, delta);
		FOG_COLOR[0] = BetterWeatherRenderer.fogColorR;
		FOG_COLOR[1] = BetterWeatherRenderer.fogColorG;
		FOG_COLOR[2] = BetterWeatherRenderer.fogColorB;
	}
}
