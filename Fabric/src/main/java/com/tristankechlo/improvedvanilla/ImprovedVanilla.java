package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ImprovedVanilla implements ModInitializer {

    @Override
    public void onInitialize() {
        // register event listeners
        UseBlockCallback.EVENT.register(CropRightClickHandler::harvestOnRightClick);
        UseBlockCallback.EVENT.register(EasyPlantingHandler::placeCropsInCircle);
        PlayerBlockBreakEvents.BEFORE.register(this::onSpawnerBroken);

        //register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ImprovedVanillaCommand.register(dispatcher);
        });

        // setup configs
        ConfigManager.loadAndVerifyConfig();
    }

    // drop spawner and spawn-eggs on block break
    private boolean onSpawnerBroken(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        return SpawnerHandler.onSpawnerBreak(world, player, pos, state, 0, (xp) -> {});
    }

}
