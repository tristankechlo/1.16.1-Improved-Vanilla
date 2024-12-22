package com.tristankechlo.improvedvanilla.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

import javax.annotation.Nullable;
import java.nio.file.Path;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the path to the directory where the configs will be placed.
     *
     * @return The path to the directory where the configs will be placed.
     */
    Path getConfigDirectory();

    void setNextSpawnData(BaseSpawner spawner, @Nullable Level level, BlockPos pos, SpawnData spawnData);

}
