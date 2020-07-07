package com.tristankechlo.improvedvanilla;

import org.apache.logging.log4j.LogManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("improvedvanilla")
public class ImprovedVanilla
{
    public static ImprovedVanilla instance;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "improvedvanilla";
    
    public ImprovedVanilla() {
        ImprovedVanilla.instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ImprovedVanillaConfig.spec);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        
        MinecraftForge.EVENT_BUS.register((Object)new SpawnerHandler());
        MinecraftForge.EVENT_BUS.register((Object)new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register((Object)new MobDropHandler());
        
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
    }
    
    @SubscribeEvent
    public void onServerStarting(final FMLServerStartingEvent event) {
    }
    
}