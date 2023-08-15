package paulevs.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import paulevs.betterweather.client.rendering.BetterWeatherRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow private Minecraft minecraft;
	@Shadow private int glEndList;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterweather_onInit(Minecraft minecraft, TextureManager manager, CallbackInfo info) {
		BetterWeatherRenderer.update(manager);
		GL11.glNewList(this.glEndList, GL11.GL_COMPILE);
		GL11.glEndList();
	}
	
	@Inject(method = "renderEnvironment", at = @At("HEAD"), cancellable = true)
	private void betterweather_renderClouds(float delta, CallbackInfo info) {
		BetterWeatherRenderer.render(delta, minecraft);
		info.cancel();
	}
	
	@ModifyArgs(method = "renderSky", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V"))
	private void betterweather_changeFogDensity(Args args) {
		BetterWeatherRenderer.changeFogDensity(args, minecraft);
	}
}
