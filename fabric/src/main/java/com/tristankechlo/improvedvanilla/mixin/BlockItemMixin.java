package com.tristankechlo.improvedvanilla.mixin;

import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place", at = @At("TAIL"))
    private void onSpawnerPlaced$improvedVanilla(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {
        SpawnerHandler.onSpawnerPlaced(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

}