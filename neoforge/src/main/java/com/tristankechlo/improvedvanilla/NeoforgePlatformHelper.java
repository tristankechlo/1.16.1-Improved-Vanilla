package com.tristankechlo.improvedvanilla;

import com.google.auto.service.AutoService;
import com.tristankechlo.improvedvanilla.mixin.BaseSpawnerAccessor;
import com.tristankechlo.improvedvanilla.mixin.CropBlockAccessor;
import com.tristankechlo.improvedvanilla.platform.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

@AutoService(IPlatformHelper.class)
public class NeoforgePlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void setNextSpawnData(BaseSpawner spawner, Level level, BlockPos pos, SpawnData spawnData) {
        ((BaseSpawnerAccessor) spawner).callSetNextSpawnData$improvedVanilla(level, pos, spawnData);
    }

    @Override
    public IntegerProperty getAgeProperty(CropBlock block) {
        return ((CropBlockAccessor) block).getAgeProperty$improvedVanilla();
    }

}
