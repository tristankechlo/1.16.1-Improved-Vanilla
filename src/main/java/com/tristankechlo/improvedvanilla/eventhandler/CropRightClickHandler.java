package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.item.ItemStack;
import java.util.List;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Collections;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropsBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CropRightClickHandler {

    @SubscribeEvent
    public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final PlayerEntity player = event.getPlayer();
        final BlockPos pos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (world.isRemote || player.isSneaking() || player.isSpectator() || event.getHand() != Hand.MAIN_HAND) {
            return;
        }
        if(ImprovedVanillaConfig.SERVER.enableRightClickCrops.get() == false) {
        	return;
        }
        if (!player.getHeldItemMainhand().isEmpty()) {
            return;
        }
        final Block targetBlock = world.getBlockState(pos).getBlock();
        IntegerProperty age;
        if (targetBlock instanceof CropsBlock) {
        	age = ((CropsBlock)targetBlock).getAgeProperty();
        } else {
            if (!targetBlock.equals(Blocks.COCOA)) {
                return;
            }
            age = CocoaBlock.AGE;
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
        final int maximumAge = Collections.max(property.getAllowedValues());
        final int currentAge = blockState.get(property);
        if (currentAge < maximumAge) {
            return false;
        }
        final BlockState newState = blockState.with(property, 0);
        world.setBlockState(pos, newState, 3);
        final List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld)world, pos, (TileEntity)null);
        drops.forEach(stack -> Block.spawnAsEntity(world, pos, stack));
        return true;
    }
}