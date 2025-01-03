package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(ImprovedVanilla.MOD_ID)
public class NeoforgeImprovedVanilla {

    public NeoforgeImprovedVanilla() {
        // register event listeners
        NeoForge.EVENT_BUS.addListener(this::cropRightClicking);
        NeoForge.EVENT_BUS.addListener(this::easyPlanting);
        NeoForge.EVENT_BUS.addListener(this::mobDropHandler);
        NeoForge.EVENT_BUS.addListener(this::onSpawnerPlaced);
        NeoForge.EVENT_BUS.addListener(this::onSpawnerBroken);

        // register commands
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        // setup configs
        NeoForge.EVENT_BUS.addListener(this::commonSetup);
    }

    // setup configs
    private void commonSetup(final ServerAboutToStartEvent event) {
        ConfigManager.loadAndVerifyConfig();
    }

    // register commands
    private void registerCommands(final RegisterCommandsEvent event) {
        ImprovedVanillaCommand.register(event.getDispatcher());
    }

    // right click crops to harvest
    private void cropRightClicking(final PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = CropRightClickHandler.onPlayerRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }

    // easy planting
    private void easyPlanting(final PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = EasyPlantingHandler.onPlayerRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }

    // drop spawn egg on entity death
    private void mobDropHandler(final LivingDropsEvent event) {
        MobDropHandler.onMobDeath(event.getEntity().level(), event.getEntity(), event.getSource(), event.getLootingLevel());
    }

    // modify spawner on placement
    private void onSpawnerPlaced(final BlockEvent.EntityPlaceEvent event) {
        InteractionResult result = SpawnerHandler.onSpawnerPlaced((Level) event.getLevel(), event.getPos());
        if (result == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }

    // drop spawner and spawn-eggs on block break
    private void onSpawnerBroken(final BlockEvent.BreakEvent event) {
        SpawnerHandler.onSpawnerBreak((Level) event.getLevel(), event.getPlayer(), event.getPos(), event.getState(), event.getExpToDrop(), event::setExpToDrop);
    }

}
