package com.tristankechlo.improvedvanilla.eventhandler;

import java.lang.reflect.Method;
import java.util.Collections;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CropRightClickHandler {

	@SubscribeEvent
	public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final EntityPlayer player = event.getEntityPlayer();
		final BlockPos pos = event.getPos();
		if (player == null || world == null) {
			return;
		}
		if (world.isRemote || player.isSneaking() || player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND) {
			return;
		}
		if (!ImprovedVanillaConfig.FARMING.enableRightClickCrops) {
			return;
		}
		if (!player.getHeldItemMainhand().isEmpty()) {
			return;
		}
		final Block targetBlock = world.getBlockState(pos).getBlock();
		PropertyInteger age = null;
		if (targetBlock instanceof BlockCrops) {
			Class<? extends Block> clazz = targetBlock.getClass();
			try {
				Method retrieveItems = clazz.getDeclaredMethod("getAgeProperty");
				retrieveItems.setAccessible(true);
				age = (PropertyInteger) retrieveItems.invoke(targetBlock);
			} catch (NoSuchMethodException e) {
				try {
					Method retrieveItems = clazz.getSuperclass().getDeclaredMethod("getAgeProperty");
					retrieveItems.setAccessible(true);
					age = (PropertyInteger) retrieveItems.invoke(targetBlock);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (targetBlock.equals(Blocks.COCOA)) {
			age = BlockCocoa.AGE;
		} else if (targetBlock.equals(Blocks.NETHER_WART)) {
			age = BlockNetherWart.AGE;
		} else {
			return;
		}

		if (age == null) {
			return;
		}

		if (this.spawnDropsAndResetBlock(world, pos, age)) {
			player.swingArm(EnumHand.MAIN_HAND);
			event.setCanceled(true);
		}
	}

	private boolean spawnDropsAndResetBlock(final World world, final BlockPos pos, final PropertyInteger property) {
		final IBlockState blockState = world.getBlockState(pos);
		if (blockState == null) {
			return false;
		}
		final int maximumAge = Collections.max(property.getAllowedValues());
		final int currentAge = blockState.getValue(property);
		if (currentAge < maximumAge) {
			return false;
		}
		final IBlockState newState = blockState.withProperty(property, 0);
		world.setBlockState(pos, newState, 3);
		final NonNullList<ItemStack> drops = NonNullList.create();
		world.getBlockState(pos).getBlock().getDrops(drops, world, pos, blockState, 0);
		drops.forEach(stack -> Block.spawnAsEntity(world, pos, stack));
		return true;
	}
}