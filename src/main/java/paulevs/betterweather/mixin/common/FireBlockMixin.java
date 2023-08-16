package paulevs.betterweather.mixin.common;

import net.minecraft.block.FireBlock;
import net.minecraft.level.Level;
import net.modificationstation.stationapi.api.block.States;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.api.WeatherAPI;

import java.util.Random;

@Mixin(FireBlock.class)
public class FireBlockMixin {
	@Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
	private void betterweather_onScheduledTick(Level level, int x, int y, int z, Random random, CallbackInfo info) {
		if (WeatherAPI.isRaining(level, x, y + 1, z)) {
			level.setBlockState(x, y, z, States.AIR.get());
			info.cancel();
		}
	}
}
