package com.tristankechlo.improvedvanilla.eventhandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.init.ModStructures;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class WorldLoadingHandler {

	private static Method GETCODEC_METHOD;

	@SuppressWarnings("resource")
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void addDimensionalSpacing(final WorldEvent.Load event) {
		if (!(event.getWorld() instanceof ServerLevel)) {
			return;
		}
		ServerLevel serverWorld = (ServerLevel) event.getWorld();

		// do not add when worldtype is terraforged
		if (isTerraForged(serverWorld)) {
			return;
		}

		// do not add when worldtype is SuperFlat
		if (serverWorld.getChunkSource().getGenerator() instanceof FlatLevelSource
				&& serverWorld.dimension().equals(Level.OVERWORLD)) {
			return;
		}

		// add structures
		Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(
				serverWorld.getChunkSource().generator.getSettings().structureConfig());
		tempMap.putIfAbsent(ModStructures.FORGOTTEN_WELL.get(), getSettings(ModStructures.FORGOTTEN_WELL.get()));
		tempMap.putIfAbsent(ModStructures.JUNGLE_TEMPLE.get(), getSettings(ModStructures.JUNGLE_TEMPLE.get()));
		tempMap.putIfAbsent(ModStructures.UNDERGROUND_TEMPLE.get(),
				getSettings(ModStructures.UNDERGROUND_TEMPLE.get()));
		serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
	}

	@SuppressWarnings("resource")
	private boolean isTerraForged(ServerLevel serverWorld) {
		try {
			if (GETCODEC_METHOD == null) {
				GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
			}
			@SuppressWarnings("unchecked")
			ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey(
					(Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(serverWorld.getChunkSource().generator));
			if (cgRL != null && cgRL.getNamespace().equals("terraforged")) {
				return true;
			}
		} catch (Exception e) {
			ImprovedVanilla.LOGGER.error("Was unable to check if " + serverWorld.dimension().location()
					+ " is using Terraforged's ChunkGenerator.");
		}
		return false;
	}

	private StructureFeatureConfiguration getSettings(StructureFeature<?> structure) {
		return StructureSettings.DEFAULTS.get(structure);
	}

}