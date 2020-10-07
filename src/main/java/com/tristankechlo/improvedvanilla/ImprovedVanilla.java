package com.tristankechlo.improvedvanilla;

import org.apache.logging.log4j.LogManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ImprovedVanilla.MOD_ID)
public class ImprovedVanilla {
	
    public static ImprovedVanilla instance;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "improvedvanilla";
    
	public static boolean SpawnerSettingsLoaded = false;
    
    public ImprovedVanilla() {
    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ImprovedVanillaConfig.spec);

    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    			
        MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnerHandler());

        ImprovedVanilla.instance = this;
        MinecraftForge.EVENT_BUS.register(this);
    }
    

    private void commonSetup(FMLCommonSetupEvent evt) {
    	ImprovedVanilla.SpawnerSettingsLoaded = ModList.get().isLoaded("spawnersettings");
    }

}