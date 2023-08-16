package paulevs.betterweather.mixin.common;

import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.util.maths.Vec2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import paulevs.betterweather.api.WeatherAPI;

import java.util.Iterator;
import java.util.Random;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow public abstract Chunk getChunkFromCache(int i, int j);
	
	@Shadow public abstract boolean addEntity(BaseEntity arg);
	
	@Shadow public abstract int getHeight(int i, int j);
	
	@Shadow public Random random;
	
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
	
	@Inject(method = "processLoadedChunks", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;getChunkFromCache(II)Lnet/minecraft/level/chunk/Chunk;",
		shift = Shift.AFTER
	), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void betterweather_processLoadedChunks(CallbackInfo info, Iterator var1, Vec2i pos) {
		if (random.nextInt(1000) > 0) return;
		int px = pos.x << 4 | random.nextInt(16);
		int pz = pos.z << 4 | random.nextInt(16);
		Level level = Level.class.cast(this);
		int py = WeatherAPI.getRainHeight(level, px, pz);
		if (!WeatherAPI.isThundering(level, px, py, pz)) return;
		this.addEntity(new LightningEntity(level, px, py, pz));
	}
}
