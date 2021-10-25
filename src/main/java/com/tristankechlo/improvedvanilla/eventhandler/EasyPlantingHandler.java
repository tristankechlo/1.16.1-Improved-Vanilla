package com.tristankechlo.improvedvanilla.eventhandler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class EasyPlantingHandler {

	// for easier access, all vanilla crops
	private final List<Item> vanillaSeeds = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT,
			Items.POTATO);

	@SubscribeEvent
	public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final Level world = event.getWorld();
		final Player player = event.getPlayer();
		final BlockPos pos = event.getPos();
		if (player == null || world == null) {
			return;
		}
		if (player.isSpectator() || event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		if (ImprovedVanillaConfig.SERVER.enableEasyPlanting.get() == false) {
			return;
		}

		final Block targetBlock = world.getBlockState(pos).getBlock();
		final Item item = player.getMainHandItem().getItem();
		final int radius = ImprovedVanillaConfig.SERVER.easyPlantingRadius.get();

		if (radius <= 0 || !(item instanceof ItemNameBlockItem)) {
			return;
		}

		if ((vanillaSeeds.contains(item) || isSeedItemForCrop(item)) && (targetBlock instanceof FarmBlock)) {
			event.setCanceled(true);
			if (world.isClientSide) {
				return;
			}
			this.setCropsInRadius(radius, pos, Blocks.FARMLAND, (ServerLevel) world, player);
			return;
		} else if ((item == Items.NETHER_WART) && (targetBlock instanceof SoulSandBlock)) {
			event.setCanceled(true);
			if (world.isClientSide) {
				return;
			}
			this.setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (ServerLevel) world, player);
			return;
		}
	}

	/**
	 * @param radius
	 * @param startPos
	 * @param target
	 * @param world
	 * @param player
	 */
	private void setCropsInRadius(int radius, BlockPos startPos, Block target, ServerLevel world, Player player) {

		List<BlockPos> targetBlocks = getTargetBlocks(radius, world, startPos, target);
		final Item seedItem = player.getMainHandItem().getItem();
		boolean playPlantingSound = false;

		for (BlockPos pos : targetBlocks) {
			// if config is set to circle and block is not inside the circle, skip this
			// block
			if (ImprovedVanillaConfig.SERVER.easyPlantingCircle.get()
					&& !isWithInCircleDistance(startPos, pos, radius)) {
				continue;
			}
			// if player has seeds -> plant the seeds
			if (playerHasOneSeed(player, seedItem)) {

				Block blockFromSeed = ForgeRegistries.BLOCKS
						.getValue(((ItemNameBlockItem) seedItem).getBlock().getRegistryName()); // get the block to
																								// place
				world.setBlockAndUpdate(pos.above(), blockFromSeed.defaultBlockState()); // set the block
				removeOneSeedFromPlayer(player, seedItem); // shrink player inv
				((ServerPlayer) player).awardStat(Stats.ITEM_USED.get(seedItem)); // increase vanilla item-use-counter

				// play sound when atleast one seed was planted
				playPlantingSound = true;
			}
		}

		// play the planting sounds
		if (player.getMainHandItem().getItem().equals(Items.NETHER_WART) && playPlantingSound) {
			world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.NETHER_WART_PLANTED,
					SoundSource.BLOCKS, 1.0F, 1.0F);
		} else if (playPlantingSound) {
			world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.CROP_PLANTED,
					SoundSource.BLOCKS, 1.0F, 1.0F);
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
	private List<BlockPos> getTargetBlocks(int radius, ServerLevel world, BlockPos startPos, Block target) {
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
				if (isTargetBlock(world, current, target) && isAir(world, current.above())
						&& !targetBlocks.contains(current)) {
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
	private boolean playerHasOneSeed(Player player, Item seed) {
		return player.getInventory().hasAnyOf(ImmutableSet.of(seed));
	}

	/**
	 * @param player
	 * @param seed
	 */
	private void removeOneSeedFromPlayer(Player player, Item seed) {
		// don't shrink player inv when in creative
		if (player.isCreative()) {
			return;
		}
		int slot = player.getInventory().findSlotMatchingUnusedItem(new ItemStack(seed));
		player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
			handler.extractItem(slot, 1, false);
			return;
		});
	}

	/**
	 * if block at pos is considered air
	 * 
	 * @param world
	 * @param pos
	 * @return
	 */
	private boolean isAir(ServerLevel world, BlockPos pos) {
		return world.getBlockState(pos).isAir();
	}

	/**
	 * compare the block at pos is equal to the provided targetblock
	 * 
	 * @param world
	 * @param pos
	 * @param target
	 * @return
	 */
	private boolean isTargetBlock(ServerLevel world, BlockPos pos, Block target) {
		return world.getBlockState(pos).is(target);
	}

	/**
	 * if the item can be used to place crops or stems
	 * 
	 * @param item
	 * @return
	 */
	private boolean isSeedItemForCrop(Item item) {
		if (!(item instanceof ItemNameBlockItem)) {
			return false;
		}
		Block block = ForgeRegistries.BLOCKS.getValue(((ItemNameBlockItem) item).getBlock().getRegistryName());
		return ((block instanceof CropBlock) || (block instanceof StemBlock));
	}

}