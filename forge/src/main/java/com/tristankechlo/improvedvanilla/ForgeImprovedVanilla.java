package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ImprovedVanilla.MOD_ID)
public class ForgeImprovedVanilla {

    public ForgeImprovedVanilla() {
        // register event listeners
        MinecraftForge.EVENT_BUS.addListener(this::cropRightClicking);
        MinecraftForge.EVENT_BUS.addListener(this::easyPlanting);
        MinecraftForge.EVENT_BUS.addListener(this::mobDropHandler);
        MinecraftForge.EVENT_BUS.addListener(this::onSpawnerPlaced);
        MinecraftForge.EVENT_BUS.addListener(this::onSpawnerBroken);

        // register commands
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        // setup configs
        MinecraftForge.EVENT_BUS.addListener(this::commonSetup);
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
        MobDropHandler.onMobDeath(event.getEntity().getLevel(), event.getEntity(), event.getSource(), event.getLootingLevel());
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
        InteractionResult result = SpawnerHandler.onSpawnerBreak((Level) event.getLevel(), event.getPlayer(), event.getPos(), event.getState(), event.getExpToDrop(), event::setExpToDrop);
        if (result == InteractionResult.SUCCESS) {
            event.setCanceled(true);
        }
    }

}
