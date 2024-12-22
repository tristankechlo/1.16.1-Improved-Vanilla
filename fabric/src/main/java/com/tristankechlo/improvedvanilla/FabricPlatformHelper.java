package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.mixin.BaseSpawnerAccessor;
import com.tristankechlo.improvedvanilla.platform.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void setNextSpawnData(BaseSpawner spawner, @Nullable Level level, BlockPos pos, SpawnData spawnData) {
        ((BaseSpawnerAccessor) spawner).callSetNextSpawnData$improvedvanilla(level, pos, spawnData);
    }

}
