package com.tristankechlo.improvedvanilla.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerInvoker {

    @Invoker("setNextSpawnData")
    void callSetNextSpawnData$improvedvanilla(Level level, BlockPos pos, SpawnData spawnData);

}
