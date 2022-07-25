package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class ImprovedVanilla implements ModInitializer {

    @Override
    public void onInitialize() {
        //FabricImprovedVanillaConfig.setup();
        // right click crops to harvest
        UseBlockCallback.EVENT.register(CropRightClickHandler::harvestOnRightClick);
        // easy planting
        UseBlockCallback.EVENT.register(EasyPlantingHandler::placeCropsInCircle);
        // drop spawn egg on entity death
        // TODO
        // modify spawner on placement
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> SpawnerHandler.onSpawnerPlaced(world, hitResult.getBlockPos()));
        // drop spawner and spawn-eggs on block break
        PlayerBlockBreakEvents.BEFORE.register((player, world, pos, state, blockEntity) -> SpawnerHandler.onSpawnerBreak(player, world, pos, state, 0, (xp) -> {}));

        // setup configs
        ConfigManager.loadAndVerifyConfig();
    }

}
