package paulevs.betterweather.mixin.common;

import net.minecraft.entity.technical.AbstractLightning;
import net.minecraft.entity.technical.LightningEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MCMath;
import net.modificationstation.stationapi.api.block.States;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.listeners.CommonListener;
import paulevs.betterweather.util.WeatherTags;

@Mixin(LightningEntity.class)
public abstract class LightningEntityMixin extends AbstractLightning {
	@Unique private boolean betterweather_isOnRod;
	
	public LightningEntityMixin(Level arg) {
		super(arg);
	}
	
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 2, ordinal = 1))
	private int betterweather_disableFireInInit(int constant) {
		int px = MCMath.floor(this.x);
		int pz = MCMath.floor(this.z);
		int py = WeatherAPI.getRainHeight(level, px, pz) - 1;
		betterweather_isOnRod = level.getBlockState(px, py, pz).isIn(WeatherTags.LIGHTNING_ROD);
		return betterweather_isOnRod ? 200 : constant;
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/level/Level;isAreaLoaded(IIII)Z"))
	private boolean betterweather_disableFireonTick(Level level, int x, int y, int z, int side) {
		return !betterweather_isOnRod && level.isAreaLoaded(x, y, z, side);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void betterweather_onInit(Level level, double x, double y, double z, CallbackInfo info) {
		int px = MCMath.floor(this.x);
		int py = MCMath.floor(this.y);
		int pz = MCMath.floor(this.z);
		if (level.getBlockState(px, py, pz).isAir()) {
			level.setBlockState(px, py, pz, CommonListener.lightningLight.getDefaultState());
			level.updateArea(px - 15, py - 15, pz - 15, px + 15, py + 15, pz + 15);
		}
	}
	
	@Inject(method = "tick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/entity/technical/LightningEntity;remove()V"
	))
	private void betterweather_onRemove(CallbackInfo info) {
		int px = MCMath.floor(this.x);
		int py = MCMath.floor(this.y);
		int pz = MCMath.floor(this.z);
		if (level.getBlockState(px, py, pz).isOf(CommonListener.lightningLight)) {
			level.setBlockState(px, py, pz, States.AIR.get());
			level.updateArea(px - 15, py - 15, pz - 15, px + 15, py + 15, pz + 15);
		}
	}
	
	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 10000.0f))
	private float betterweather_changeThunderDistance(float constant) {
		return 200F;
	}
	
	@Redirect(method = "tick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;playSound(DDDLjava/lang/String;FF)V",
		ordinal = 1
	))
	private void betterweather_disableExplosionSound(Level level, double e, double f, double string, String g, float h, float v) {}
}
