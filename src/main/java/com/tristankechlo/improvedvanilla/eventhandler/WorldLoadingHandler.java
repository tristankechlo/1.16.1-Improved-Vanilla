package com.tristankechlo.improvedvanilla.eventhandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.init.ModStructures;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class WorldLoadingHandler {

	private static Method GETCODEC_METHOD;

	@SuppressWarnings("resource")
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void addDimensionalSpacing(final WorldEvent.Load event) {
		if (!(event.getWorld() instanceof ServerWorld)) {
			return;
		}
		ServerWorld serverWorld = (ServerWorld) event.getWorld();

		// do not add when worldtype is terraforged
		if (isTerraForged(serverWorld)) {
			return;
		}

		// do not add when worldtype is SuperFlat
		if (serverWorld.getChunkSource().getGenerator() instanceof FlatChunkGenerator
				&& serverWorld.dimension().equals(World.OVERWORLD)) {
			return;
		}

		// add structures
		Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(
				serverWorld.getChunkSource().generator.getSettings().structureConfig());
		tempMap.putIfAbsent(ModStructures.FORGOTTEN_WELL.get(), getSettings(ModStructures.FORGOTTEN_WELL.get()));
		tempMap.putIfAbsent(ModStructures.JUNGLE_TEMPLE.get(), getSettings(ModStructures.JUNGLE_TEMPLE.get()));
		serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
	}

	@SuppressWarnings("resource")
	private boolean isTerraForged(ServerWorld serverWorld) {
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

	private StructureSeparationSettings getSettings(Structure<?> structure) {
		return DimensionStructuresSettings.DEFAULTS.get(structure);
	}

}
