package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class ImprovedVanilla implements ModInitializer {

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register(CropRightClickHandler::onPlayerRightClickBlock);
    }

}
