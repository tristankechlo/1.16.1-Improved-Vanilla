package com.tristankechlo.improvedvanilla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ImprovedVanilla.MOD_ID, acceptableRemoteVersions = "*", acceptedMinecraftVersions="[1.12.2,1.13)")
public class ImprovedVanilla {

	public static Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "improvedvanilla";

	public ImprovedVanilla() {
		MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
		MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
		MinecraftForge.EVENT_BUS.register(new MobDropHandler());
		MinecraftForge.EVENT_BUS.register(new SpawnerHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER = event.getModLog();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ConfigManager.sync(MOD_ID, Type.INSTANCE);
	}

}