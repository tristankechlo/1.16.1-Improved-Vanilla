package com.tristankechlo.improvedvanilla.eventhandler;

import java.util.Collections;
import java.util.List;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CropRightClickHandler {
	
    @SubscribeEvent
    public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final PlayerEntity player = event.getPlayer();
        final BlockPos pos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (world.isClientSide || player.isShiftKeyDown() || player.isSpectator() || event.getHand() != Hand.MAIN_HAND) {
            return;
        }
        if(ImprovedVanillaConfig.SERVER.enableRightClickCrops.get() == false) {
        	return;
        }
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }
        final Block targetBlock = world.getBlockState(pos).getBlock();
        IntegerProperty age;
        if (targetBlock instanceof CropsBlock) {
        	age = ((CropsBlock)targetBlock).getAgeProperty();
        } else if (targetBlock.equals(Blocks.COCOA)) {
            age = CocoaBlock.AGE;
        } else if (targetBlock.equals(Blocks.NETHER_WART)) {
            age = NetherWartBlock.AGE;
        } else {
            return;
        }
        
        if (this.spawnDropsAndResetBlock(world, pos, age)) {
            player.swing(Hand.MAIN_HAND, true);
            event.setCanceled(true);
        }
    }
    
    private boolean spawnDropsAndResetBlock(final World world, final BlockPos pos, final IntegerProperty property) {
        final BlockState blockState = world.getBlockState(pos);
        if (blockState == null) {
            return false;
        }
        final int maximumAge = Collections.max(property.getPossibleValues());
        final int currentAge = blockState.getValue(property);
        if (currentAge < maximumAge) {
            return false;
        }
        final BlockState newState = blockState.setValue(property, 0);
        world.setBlock(pos, newState, 3);
        final List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld)world, pos, (TileEntity)null);
        drops.forEach(stack -> Block.popResource(world, pos, stack));
        return true;
    }
}