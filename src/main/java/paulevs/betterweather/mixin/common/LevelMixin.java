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
	
	/*@Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
	private void betterweather_getRainGradient(float delta, CallbackInfoReturnable<Float> info) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			@SuppressWarnings("deprecation")
			Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
			if (minecraft.viewEntity != null && minecraft.level != null) {
				float value = WeatherAPI.isRaining(
					minecraft.level,
					MathHelper.floor(minecraft.viewEntity.x),
					MathHelper.floor(minecraft.viewEntity.y),
					MathHelper.floor(minecraft.viewEntity.z)
				) ? 1F : 0F;
				info.setReturnValue(value);
			}
		}
		info.setReturnValue(0F);
	}*/
	
	/*@ModifyVariable(
		method = "getSkyColor(Lnet/minecraft/entity/BaseEntity;F)Lnet/minecraft/util/maths/Vec3f;",
		at = @At(value = "STORE"), ordinal = 12, argsOnly = true
	)
	private float betterweather_changeSkyColor(float x) {
		return 1F;
	}*/
	
	/*@Inject(method = "getSkyColor", at = )
	private void betterweather_changeSkyColor() {
	
	}*/
}
