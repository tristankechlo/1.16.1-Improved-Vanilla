package com.tristankechlo.improvedvanilla.init;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.structures.ForgottenWellStructure;
import com.tristankechlo.improvedvanilla.structures.JungleTempleStructure;
import com.tristankechlo.improvedvanilla.structures.UnderGroundTempleStructure;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModStructures {

	public static final DeferredRegister<StructureFeature<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, ImprovedVanilla.MOD_ID);

	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> FORGOTTEN_WELL = STRUCTURES
			.register("forgotten_well", () -> (new ForgottenWellStructure(NoneFeatureConfiguration.CODEC)));
	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> JUNGLE_TEMPLE = STRUCTURES
			.register("jungle_temple", () -> (new JungleTempleStructure(NoneFeatureConfiguration.CODEC)));
	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> UNDERGROUND_TEMPLE = STRUCTURES
			.register("underground_temple", () -> (new UnderGroundTempleStructure(NoneFeatureConfiguration.CODEC)));

	public static void setupStructures() {
		setupMapSpacingAndLand(FORGOTTEN_WELL.get(), new StructureFeatureConfiguration(10, 5, 2147413647), false);
		setupMapSpacingAndLand(JUNGLE_TEMPLE.get(), new StructureFeatureConfiguration(17, 6, 2147413646), false);
		setupMapSpacingAndLand(UNDERGROUND_TEMPLE.get(), new StructureFeatureConfiguration(15, 6, 2147413645), false);
	}

	private static <F extends StructureFeature<?>> void setupMapSpacingAndLand(F structure,
			StructureFeatureConfiguration structureSeparationSettings, boolean transformSurroundingLand) {
		StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

		if (transformSurroundingLand) {
			StructureFeature.NOISE_AFFECTING_FEATURES = ImmutableList.<StructureFeature<?>>builder()
					.addAll(StructureFeature.NOISE_AFFECTING_FEATURES).add(structure).build();
		}

		StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
				.putAll(StructureSettings.DEFAULTS).put(structure, structureSeparationSettings).build();

		BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
			Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.getValue()
					.structureSettings().structureConfig();

			if (structureMap instanceof ImmutableMap) {
				Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
				tempMap.put(structure, structureSeparationSettings);
				settings.getValue().structureSettings().structureConfig = tempMap;
			} else {
				structureMap.put(structure, structureSeparationSettings);
			}
		});
	}

}