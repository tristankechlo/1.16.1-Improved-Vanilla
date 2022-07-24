package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ImprovedVanilla {

    public ImprovedVanilla() {
        MinecraftForge.EVENT_BUS.addListener(this::cropRightClicking);
        MinecraftForge.EVENT_BUS.addListener(this::easyPlanting);
    }

    private void cropRightClicking(final PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = CropRightClickHandler.onPlayerRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result == InteractionResult.SUCCESS) {
            event.getEntity().swing(event.getHand(), true);
            event.setCanceled(true);
        }
    }

    private void easyPlanting(final PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = EasyPlantingHandler.onPlayerRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result == InteractionResult.SUCCESS) {
            event.getEntity().swing(event.getHand(), true);
            event.setCanceled(true);
        }
    }

}