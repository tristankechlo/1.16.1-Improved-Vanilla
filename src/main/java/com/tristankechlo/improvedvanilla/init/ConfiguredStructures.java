package com.tristankechlo.improvedvanilla.init;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ConfiguredStructures {

	public static ConfiguredStructureFeature<?, ?> CONFIGURED_FORGOTTEN_WELL = ModStructures.FORGOTTEN_WELL.get()
			.configured(NoneFeatureConfiguration.INSTANCE);
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_JUNGLE_TEMPLE = ModStructures.JUNGLE_TEMPLE.get()
			.configured(NoneFeatureConfiguration.INSTANCE);
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_UNDERGROUND_TEMPLE = ModStructures.UNDERGROUND_TEMPLE
			.get().configured(NoneFeatureConfiguration.INSTANCE);

	public static void registerConfiguredStructures() {
		Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_forgotten_well"),
				CONFIGURED_FORGOTTEN_WELL);
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_jungle_temple"),
				CONFIGURED_JUNGLE_TEMPLE);
		Registry.register(registry, new ResourceLocation(ImprovedVanilla.MOD_ID, "configured_underground_temple"),
				CONFIGURED_UNDERGROUND_TEMPLE);

//		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.FORGOTTEN_WELL.get(), CONFIGURED_FORGOTTEN_WELL);
//		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.JUNGLE_TEMPLE.get(), CONFIGURED_JUNGLE_TEMPLE);
//		FlatGenerationSettings.STRUCTURE_FEATURES.put(ModStructures.UNDERGROUND_TEMPLE.get(),
//				CONFIGURED_UNDERGROUND_TEMPLE);
	}
}