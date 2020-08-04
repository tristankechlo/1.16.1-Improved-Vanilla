package com.tristankechlo.improvedvanilla;

import org.apache.logging.log4j.LogManager;

import net.minecraftforge.eventbus.api.IEventBus;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.client.ClientSetup;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import com.tristankechlo.improvedvanilla.init.ModContainerTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("improvedvanilla")
public class ImprovedVanilla {
	
    public static ImprovedVanilla instance;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "improvedvanilla";
    
    public ImprovedVanilla() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ImprovedVanillaConfig.spec);
        

		ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
		
		
        MinecraftForge.EVENT_BUS.register(new SpawnerHandler());
        MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());   

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

        ImprovedVanilla.instance = this;
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

}