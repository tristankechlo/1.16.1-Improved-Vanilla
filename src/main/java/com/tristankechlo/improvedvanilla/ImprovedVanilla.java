package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImprovedVanilla.MOD_ID)
public class ImprovedVanilla {

    public static final String MOD_ID = "improvedvanilla";
    public static final String MOD_NAME = "Improved Vanilla";
    public final static Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public ImprovedVanilla() {
        MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnerHandler());

        // register commands
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        // setup configs
        MinecraftForge.EVENT_BUS.addListener(this::commonSetup);
    }

    // setup configs
    private void commonSetup(final FMLServerAboutToStartEvent event) {
        ConfigManager.loadAndVerifyConfig();
    }

    // register commands
    private void registerCommands(final FMLServerStartingEvent event) {
        ImprovedVanillaCommand.register(event.getCommandDispatcher());
    }

}