package paulevs.betterweather.mixin.common;

import net.minecraft.entity.AbstractLightning;
import net.minecraft.entity.LightningEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.betterweather.api.WeatherAPI;
import paulevs.betterweather.util.WeatherTags;

@Mixin(LightningEntity.class)
public abstract class LightningEntityMixin extends AbstractLightning {
	@Unique private boolean betterweather_isOnRod;
	
	public LightningEntityMixin(Level arg) {
		super(arg);
	}
	
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 2, ordinal = 1))
	private int betterweather_onInit(int constant) {
		int px = MathHelper.floor(this.x);
		int pz = MathHelper.floor(this.z);
		int py = WeatherAPI.getRainHeight(level, px, pz) - 1;
		betterweather_isOnRod = level.getBlockState(px, py, pz).isIn(WeatherTags.LIGHTNING_ROD);
		return betterweather_isOnRod ? 200 : constant;
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/level/Level;isAreaLoaded(IIII)Z"))
	private boolean injected(Level level, int x, int y, int z, int side) {
		return !betterweather_isOnRod && level.isAreaLoaded(x, y, z, side);
	}
}
