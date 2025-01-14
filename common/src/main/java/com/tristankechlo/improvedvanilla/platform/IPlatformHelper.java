package com.tristankechlo.improvedvanilla.platform;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.nio.file.Path;

public interface IPlatformHelper {

    IPlatformHelper INSTANCE = ImprovedVanilla.load(IPlatformHelper.class);

    Path getConfigDirectory();

    void setNextSpawnData(BaseSpawner spawner, Level level, BlockPos pos, SpawnData spawnData);

    IntegerProperty getAgeProperty(CropBlock block);

}
