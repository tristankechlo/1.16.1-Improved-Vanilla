package com.tristankechlo.improvedvanilla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.eventhandler.BiomeLoadingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import com.tristankechlo.improvedvanilla.eventhandler.WorldLoadingHandler;
import com.tristankechlo.improvedvanilla.init.ConfiguredStructures;
import com.tristankechlo.improvedvanilla.init.ModStructures;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImprovedVanilla.MOD_ID)
public class ImprovedVanilla {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "improvedvanilla";

	public ImprovedVanilla() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImprovedVanillaConfig.spec);

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		ModStructures.STRUCTURES.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
		MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
		MinecraftForge.EVENT_BUS.register(new MobDropHandler());
		MinecraftForge.EVENT_BUS.register(new SpawnerHandler());

		MinecraftForge.EVENT_BUS.register(new BiomeLoadingHandler());
		MinecraftForge.EVENT_BUS.register(new WorldLoadingHandler());

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ModStructures.setupStructures();
			ConfiguredStructures.registerConfiguredStructures();
		});
	}

}