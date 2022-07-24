package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class ImprovedVanilla implements ModInitializer {

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register(CropRightClickHandler::onPlayerRightClickBlock);
        UseBlockCallback.EVENT.register(EasyPlantingHandler::onPlayerRightClickBlock);
    }

}
