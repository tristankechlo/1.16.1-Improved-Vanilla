package com.tristankechlo.improvedvanilla.init;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.structures.ForgottenWellStructure;
import com.tristankechlo.improvedvanilla.structures.JungleTempleStructure;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModStructures {

	public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, ImprovedVanilla.MOD_ID);

	public static final RegistryObject<Structure<NoFeatureConfig>> FORGOTTEN_WELL = STRUCTURES
			.register("forgotten_well", () -> (new ForgottenWellStructure(NoFeatureConfig.CODEC)));
	public static final RegistryObject<Structure<NoFeatureConfig>> JUNGLE_TEMPLE = STRUCTURES
			.register("jungle_temple", () -> (new JungleTempleStructure(NoFeatureConfig.CODEC)));

	public static void setupStructures() {
		setupMapSpacingAndLand(FORGOTTEN_WELL.get(), new StructureSeparationSettings(10, 5, 1234567890), true);
		setupMapSpacingAndLand(JUNGLE_TEMPLE.get(), new StructureSeparationSettings(25, 7, 1933467234), false);
	}

	private static <F extends Structure<?>> void setupMapSpacingAndLand(F structure,
			StructureSeparationSettings structureSeparationSettings, boolean transformSurroundingLand) {
		Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

		if (transformSurroundingLand) {
			Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>>builder()
					.addAll(Structure.NOISE_AFFECTING_FEATURES).add(structure).build();
		}

		DimensionStructuresSettings.DEFAULTS = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
				.putAll(DimensionStructuresSettings.DEFAULTS).put(structure, structureSeparationSettings).build();

		WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
			Map<Structure<?>, StructureSeparationSettings> structureMap = settings.getValue().structureSettings()
					.structureConfig();

			if (structureMap instanceof ImmutableMap) {
				Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
				tempMap.put(structure, structureSeparationSettings);
				settings.getValue().structureSettings().structureConfig = tempMap;
			} else {
				structureMap.put(structure, structureSeparationSettings);
			}
		});
	}

}
