package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.init.ConfiguredStructures;
import com.tristankechlo.improvedvanilla.structures.ForgottenWellStructure;
import com.tristankechlo.improvedvanilla.structures.JungleTempleStructure;
import com.tristankechlo.improvedvanilla.structures.UnderGroundTempleStructure;

import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BiomeLoadingHandler {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void biomeModification(final BiomeLoadingEvent event) {

		// add forgotten well biomes
		if (ForgottenWellStructure.DEFAULT_BIOMES.contains(event.getName().toString())) {
			event.getGeneration().getStructures().add(() -> ConfiguredStructures.CONFIGURED_FORGOTTEN_WELL);
		}

		// add jungle temple biomes
		if (JungleTempleStructure.DEFAULT_BIOMES.contains(event.getName().toString())) {
			event.getGeneration().getStructures().add(() -> ConfiguredStructures.CONFIGURED_JUNGLE_TEMPLE);
		}

		// add underground temple biomes
		if (UnderGroundTempleStructure.DEFAULT_BIOMES.contains(event.getName().toString())) {
			event.getGeneration().getStructures().add(() -> ConfiguredStructures.CONFIGURED_UNDERGROUND_TEMPLE);
		}

	}

}
