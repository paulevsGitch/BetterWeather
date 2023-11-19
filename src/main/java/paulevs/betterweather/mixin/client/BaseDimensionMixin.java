package paulevs.betterweather.mixin.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.betterweather.api.WeatherAPI;

@Mixin(Dimension.class)
public class BaseDimensionMixin {
	@Inject(method = "getSunsetDawnColor", at = @At("RETURN"))
	private void betterweather_getSunsetDawnColor(float f, float g, CallbackInfoReturnable<float[]> info) {
		float[] data = info.getReturnValue();
		if (data == null) return;
		@SuppressWarnings("deprecation")
		Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
		LivingEntity entity = minecraft.viewEntity;
		data[3] *= 1F - WeatherAPI.getRainDensity(minecraft.level, entity.x, entity.y, entity.z, true);
	}
}
