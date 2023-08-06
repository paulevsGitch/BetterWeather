package paulevs.betterweather.mixin.common;

import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.betterweather.api.WeatherAPI;

@Mixin(Level.class)
public class LevelMixin {
	@Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
	private void betterweather_isRaining(CallbackInfoReturnable<Boolean> info)  {
		info.setReturnValue(false);
	}
	
	@Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
	private void betterweather_isThundering(CallbackInfoReturnable<Boolean> info)  {
		info.setReturnValue(false);
	}
	
	@Inject(method = "canRainAt", at = @At("HEAD"), cancellable = true)
	private void betterweather_isRainingAt(int x, int y, int z, CallbackInfoReturnable<Boolean> info)  {
		info.setReturnValue(WeatherAPI.isRaining(Level.class.cast(this), x, y, z));
	}
}
