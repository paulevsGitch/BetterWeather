package paulevs.betterweather.util;

import net.minecraft.block.BaseBlock;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.DimensionContainer;
import net.modificationstation.stationapi.api.registry.DimensionRegistry;
import net.modificationstation.stationapi.api.registry.ModID;
import net.modificationstation.stationapi.api.tag.TagKey;

public class WeatherTags {
	public static final TagKey<BaseBlock> LIGHTNING_ROD;
	public static final TagKey<DimensionContainer<?>> NO_RAIN;
	public static final TagKey<DimensionContainer<?>> NO_THUNDER;
	public static final TagKey<DimensionContainer<?>> ETERNAL_RAIN;
	public static final TagKey<DimensionContainer<?>> ETERNAL_THUNDER;
	
	static {
		ModID modID = ModID.of("better_weather");
		LIGHTNING_ROD = TagKey.of(BlockRegistry.KEY, modID.id("lightning_rod"));
		NO_RAIN = TagKey.of(DimensionRegistry.KEY, modID.id("no_rain"));
		NO_THUNDER = TagKey.of(DimensionRegistry.KEY, modID.id("no_thunder"));
		ETERNAL_RAIN = TagKey.of(DimensionRegistry.KEY, modID.id("eternal_rain"));
		ETERNAL_THUNDER = TagKey.of(DimensionRegistry.KEY, modID.id("eternal_thunder"));
	}
}
