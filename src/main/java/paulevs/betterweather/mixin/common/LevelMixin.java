package paulevs.betterweather.mixin.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.Vec2I;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.util.LightningUtil;

import java.util.Iterator;
import java.util.Random;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Unique private boolean betterweather_flag;
	
	@Shadow public Random random;
	
	@Shadow public abstract boolean addEntity(Entity arg);
	
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
	
	@SuppressWarnings("rawtypes")
	@Inject(method = "processLoadedChunks", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;getChunkFromCache(II)Lnet/minecraft/level/chunk/Chunk;",
		shift = Shift.AFTER
	), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void betterweather_processLoadedChunks(CallbackInfo info, Iterator iterator, Vec2I pos) {
		LightningUtil.processChunk(Level.class.cast(this), pos.x, pos.z);
	}
	
	@Inject(method = "processLoadedChunks", at = @At("HEAD"))
	private void betterweather_tick(CallbackInfo info) {
		LightningUtil.tick();
	}
	
	@Environment(EnvType.CLIENT)
	@Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
	private void betterweather_getRainGradient(float delta, CallbackInfoReturnable<Float> info) {
		if (betterweather_flag) {
			betterweather_flag = false;
			info.setReturnValue(0F);
			return;
		}
		
		@SuppressWarnings("deprecation")
		Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
		LivingEntity entity = minecraft.viewEntity;
		if (entity == null || minecraft.level == null) return;
		info.setReturnValue(WeatherAPI.getRainDensity(minecraft.level, entity.x, entity.y, entity.z, true));
	}
	
	@Inject(method = "getEnvironmentLight", at = @At("HEAD"))
	private void betterweather_getEnvironmentLight(float delta, CallbackInfoReturnable<Integer> info) {
		betterweather_flag = true;
	}
}
