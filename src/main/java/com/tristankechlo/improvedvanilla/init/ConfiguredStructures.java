package com.tristankechlo.improvedvanilla.init;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class ConfiguredStructures {

	public static StructureFeature<?, ?> CONFIGURED_FORGOTTEN_WELL = ModStructures.FORGOTTEN_WELL.get()
			.configured(IFeatureConfig.NONE);
	public static StructureFeature<?, ?> CONFIGURED_JUNGLE_TEMPLE = ModStructures.JUNGLE_TEMPLE.get()
			.configured(IFeatureConfig.NONE);
	public static StructureFeature<?, ?> CONFIGURED_UNDERGROUND_TEMPLE = ModStructures.UNDERGROUND_TEMPLE.get()
			.configured(IFeatureConfig.NONE);

	public static void registerConfiguredStructures() {
		Registry<StructureFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_forgotten_well"),
				CONFIGURED_FORGOTTEN_WELL);
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_jungle_temple"),
				CONFIGURED_JUNGLE_TEMPLE);
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_underground_temple"),
				CONFIGURED_UNDERGROUND_TEMPLE);

		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.FORGOTTEN_WELL.get(), CONFIGURED_FORGOTTEN_WELL);
		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.JUNGLE_TEMPLE.get(), CONFIGURED_JUNGLE_TEMPLE);
		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.UNDERGROUND_TEMPLE.get(), CONFIGURED_UNDERGROUND_TEMPLE);
	}
}
