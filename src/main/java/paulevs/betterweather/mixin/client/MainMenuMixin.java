package paulevs.betterweather.mixin.client;

import net.minecraft.client.gui.screen.menu.MainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.sound.WeatherSounds;

@Mixin(MainMenu.class)
public class MainMenuMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterweather_onInit(CallbackInfo info) {
		WeatherSounds.stop();
	}
}
