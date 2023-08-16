package paulevs.betterweather.mixin.client;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LightningRenderer;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.client.rendering.BWLightningRenderer;

@Mixin(LightningRenderer.class)
public abstract class LightningRendererMixin extends EntityRenderer {
	@Inject(method = "method_1793", at = @At("HEAD"), cancellable = true)
	private void betterweather_render(LightningEntity entity, double dx, double dy, double dz, float delta, float h, CallbackInfo info) {
		BWLightningRenderer.render(entity, (float) dx, (float) dy, (float) dz, dispatcher.textureManager);
		info.cancel();
	}
}
