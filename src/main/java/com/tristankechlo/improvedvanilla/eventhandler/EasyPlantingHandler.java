package com.tristankechlo.improvedvanilla.eventhandler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;

public class EasyPlantingHandler {

	// for easier access, all vanilla crops
	private final List<Item> vanillaSeeds = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO);

	@SubscribeEvent
	public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final EntityPlayer player = event.getEntityPlayer();
		final BlockPos pos = event.getPos();
		if (player == null || world == null) {
			return;
		}
		if (player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND || player instanceof EntityPlayerSP) {
			return;
		}
		if (!ImprovedVanillaConfig.FARMING.enableEasyPlanting) {
			return;
		}

		final Block targetBlock = world.getBlockState(pos).getBlock();
		final Item item = player.getHeldItemMainhand().getItem();
		final int radius = ImprovedVanillaConfig.FARMING.easyPlantingRadius;

		if (radius <= 0 || (!(item instanceof ItemSeeds) && !(item instanceof ItemSeedFood))) {
			return;
		}

		if (vanillaSeeds.contains(item) && (targetBlock instanceof BlockFarmland) && (getPlantType(world, pos, item) == EnumPlantType.Crop)) {
			event.setCanceled(true);
			if (world.isRemote) {
				return;
			}
			this.setCropsInRadius(radius, pos, Blocks.FARMLAND, (WorldServer) world, player);
			return;
		} else if ((targetBlock instanceof BlockSoulSand) && (getPlantType(world, pos, item) == EnumPlantType.Nether)) {
			event.setCanceled(true);
			if (world.isRemote) {
				return;
			}
			this.setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (WorldServer) world, player);
			return;
		}
	}

	private static EnumPlantType getPlantType(World world, BlockPos pos, Item item) {
		if (item instanceof ItemSeeds) {
			return ((ItemSeeds) item).getPlantType(world, pos);
		}
		if (item instanceof ItemSeedFood) {
			return ((ItemSeedFood) item).getPlantType(world, pos);
		}
		return null;
	}

	private static IBlockState getPlant(World world, BlockPos pos, Item item) {
		if (item instanceof ItemSeeds) {
			return ((ItemSeeds) item).getPlant(world, pos);
		}
		if (item instanceof ItemSeedFood) {
			return ((ItemSeedFood) item).getPlant(world, pos);
		}
		return null;
	}

	/**
	 * @param radius
	 * @param startPos
	 * @param target
	 * @param world
	 * @param player
	 */
	private void setCropsInRadius(int radius, BlockPos startPos, Block target, WorldServer world, EntityPlayer player) {

		List<BlockPos> targetBlocks = getTargetBlocks(radius, world, startPos, target);
		final Item seedItem = player.getHeldItemMainhand().getItem();
		boolean playPlantingSound = false;

		for (BlockPos pos : targetBlocks) {
			// if config is set to circle and block is not inside the circle, skip this block
			if (ImprovedVanillaConfig.FARMING.easyPlantingCircle && !isWithInCircleDistance(startPos, pos, radius)) {
				continue;
			}
			// if player has seeds -> plant the seeds
			if (playerHasOneSeed(player, seedItem)) {

				world.setBlockState(pos.up(), getPlant(world, startPos, seedItem));
				removeOneSeedFromPlayer(player, seedItem);
				// play sound when atleast one seed was planted
				playPlantingSound = true;
			}
		}

		// play the planting sounds
		if (playPlantingSound) {
			world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	/**
	 * get all blocks in a radius using floodfill
	 * 
	 * @param world
	 * @param startPos
	 * @param target
	 * @param radius
	 * @return
	 */
	private List<BlockPos> getTargetBlocks(int radius, WorldServer world, BlockPos startPos, Block target) {
		List<BlockPos> targetBlocks = new ArrayList<BlockPos>();
		Queue<Point> queue = new LinkedList<Point>();
		queue.add(new Point(startPos.getX(), startPos.getZ()));
		final int minX = startPos.getX() - radius;
		final int maxX = startPos.getX() + radius;
		final int minY = startPos.getZ() - radius;
		final int maxY = startPos.getZ() + radius;

		while (!queue.isEmpty()) {
			Point p = queue.remove();
			// if inside of square
			if ((p.x >= minX) && (p.x <= maxX) && (p.y >= minY) && (p.y <= maxY)) {
				BlockPos current = new BlockPos(p.x, startPos.getY(), p.y);
				// if current block is can be used to plant the crop
				if (isTargetBlock(world, current, target) && isAir(world, current.up()) && !targetBlocks.contains(current)) {
					targetBlocks.add(current);
					queue.add(new Point(p.x + 1, p.y));
					queue.add(new Point(p.x - 1, p.y));
					queue.add(new Point(p.x, p.y + 1));
					queue.add(new Point(p.x, p.y - 1));
				}
			}
		}
		return targetBlocks;
	}

	/**
	 * wether or not the endpos is the radius for the startpos
	 * 
	 * @param start
	 * @param end
	 * @param radius
	 * @return
	 */
	private boolean isWithInCircleDistance(BlockPos start, BlockPos end, int radius) {
		double x = Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getZ() - end.getZ()), 2));
		return x <= (radius + 0.5);
	}

	/**
	 * wether or not the player has atleast one specified seed item
	 * 
	 * @param player
	 * @param seed
	 * @return
	 */
	private boolean playerHasOneSeed(EntityPlayer player, Item seed) {
		return player.inventory.hasItemStack(new ItemStack(seed));
	}

	/**
	 * @param player
	 * @param seed
	 */
	private void removeOneSeedFromPlayer(EntityPlayer player, Item seed) {
		// don't shrink player inv when in creative
		if (player.isCreative()) {
			return;
		}
		int slot = player.inventory.findSlotMatchingUnusedItem(new ItemStack(seed));
		player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(slot, 1, false);
	}

	/**
	 * if block at pos is considered air
	 * 
	 * @param world
	 * @param pos
	 * @return
	 */
	private boolean isAir(WorldServer world, BlockPos pos) {
		return world.getBlockState(pos).getBlock().equals(Blocks.AIR);
	}

	/**
	 * compare the block at pos is equal to the provided targetblock
	 * 
	 * @param world
	 * @param pos
	 * @param target
	 * @return
	 */
	private boolean isTargetBlock(WorldServer world, BlockPos pos, Block target) {
		return world.getBlockState(pos).getBlock().equals(target);
	}

}