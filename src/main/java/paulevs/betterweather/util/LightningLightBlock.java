package paulevs.betterweather.util;

import net.minecraft.block.material.Material;
import net.minecraft.entity.technical.LightningEntity;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.Vec3D;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.block.States;
import net.modificationstation.stationapi.api.item.ItemPlacementContext;
import net.modificationstation.stationapi.api.template.block.TemplateBlock;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Random;

public class LightningLightBlock extends TemplateBlock {
	public LightningLightBlock(Identifier id) {
		super(id, Material.AIR);
		setHardness(0);
		setBlastResistance(0);
		setTranslationKey("minecraft:air");
		disableNotifyOnMetaDataChange();
		disableStat();
		setLightOpacity(0);
		setLightEmittance(1);
		setTicksRandomly(true);
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public boolean isSideRendered(BlockView tileView, int x, int y, int z, int side) {
		return false;
	}
	
	@Override
	public Box getOutlineShape(Level level, int x, int y, int z) {
		return null;
	}
	
	@Override
	public Box getCollisionShape(Level level, int x, int y, int z) {
		return null;
	}
	
	@Override
	public boolean isFullOpaque() {
		return false;
	}
	
	@Override
	public boolean isCollidable(int meta, boolean flag) {
		return false;
	}
	
	@Override
	public int getDropCount(Random rand) {
		return 0;
	}
	
	@Override
	public HitResult getHitResult(Level level, int x, int y, int z, Vec3D arg1, Vec3D arg2) {
		return null;
	}
	
	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		return true;
	}
	
	@Override
	public void onScheduledTick(Level level, int x, int y, int z, Random random) {
		if (level.getEntities(LightningEntity.class, Box.createAndCache(x, y, z, x, y, z)).isEmpty()) {
			level.setBlockState(x, y, z, States.AIR.get());
			level.updateArea(x - 15, y - 15, z - 15, x + 15, y + 15, z + 15);
		}
	}
}
