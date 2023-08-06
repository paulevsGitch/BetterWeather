package paulevs.betterweather.mixin.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.sound.WeatherSounds;
import paulscode.sound.SoundSystem;

@Mixin(SoundHelper.class)
public class SoundHelperMixin {
	@Shadow private static boolean initialized;
	@Shadow private static SoundSystem soundSystem;
	
	@Inject(method = "handleBackgroundMusic", at = @At("HEAD"), cancellable = true)
	private void bnb_handleBackgroundMusic(CallbackInfo info) {
		if (!initialized) return;
		@SuppressWarnings("deprecated")
		Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
		WeatherSounds.updateSound(minecraft.level, minecraft.viewEntity, soundSystem);
	}
}
