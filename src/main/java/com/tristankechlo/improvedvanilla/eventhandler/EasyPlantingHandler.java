package com.tristankechlo.improvedvanilla.eventhandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EasyPlantingHandler {

    //for easier access, all vanilla crops
    private final List<Item> vanillaSeeds = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO);

    @SubscribeEvent
    public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final PlayerEntity player = event.getPlayer();
        final BlockPos pos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (player.isSpectator() || event.getHand() != Hand.MAIN_HAND) {
            return;
        }
        if (ImprovedVanillaConfig.SERVER.enableEasyPlanting.get() == false) {
            return;
        }

        final Block targetBlock = world.getBlockState(pos).getBlock();
        final Item item = player.getMainHandItem().getItem();
        final int radius = ImprovedVanillaConfig.SERVER.easyPlantingRadius.get();

        if (radius <= 0 || !(item instanceof BlockNamedItem)) {
            return;
        }

        if ((vanillaSeeds.contains(item) || isSeedItemForCrop(item)) && (targetBlock instanceof FarmlandBlock)) {
            event.setCanceled(true);
            if (world.isClientSide()) {
                return;
            }
            this.setCropsInRadius(radius, pos, Blocks.FARMLAND, (ServerWorld) world, player);
            return;
        } else if ((item == Items.NETHER_WART) && (targetBlock instanceof SoulSandBlock)) {
            event.setCanceled(true);
            if (world.isClientSide()) {
                return;
            }
            this.setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (ServerWorld) world, player);
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
    private void setCropsInRadius(int radius, BlockPos startPos, Block target, ServerWorld world, PlayerEntity player) {

        List<BlockPos> targetBlocks = getTargetBlocks(radius, world, startPos, target);
        final Item seedItem = player.getMainHandItem().getItem();
        boolean playPlantingSound = false;

        for (BlockPos pos : targetBlocks) {
            //if config is set to circle and block is not inside the circle, skip this block
            if (ImprovedVanillaConfig.SERVER.easyPlantingCircle.get() && !isWithInCircleDistance(startPos, pos, radius)) {
                continue;
            }
            //if player has seeds -> plant the seeds
            if (playerHasOneSeed(player, seedItem)) {

                Block blockFromSeed = ForgeRegistries.BLOCKS.getValue(((BlockNamedItem) seedItem).getBlock().getRegistryName());        //get the block to place
                world.setBlockAndUpdate(pos.above(), blockFromSeed.defaultBlockState());                                                        //set the block
                removeOneSeedFromPlayer(player, seedItem);                                                                            //shrink player inv
                player.awardStat(Stats.ITEM_USED.get(seedItem));                                                //increase vanilla item-use-counter

                //play sound when atleast one seed was planted
                playPlantingSound = true;
            }
        }

        //play the planting sounds
        if (player.getMainHandItem().getItem().equals(Items.NETHER_WART) && playPlantingSound) {
            world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.NETHER_WART_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else if (playPlantingSound) {
            world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.CROP_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
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
    private List<BlockPos> getTargetBlocks(int radius, ServerWorld world, BlockPos startPos, Block target) {
        List<BlockPos> targetBlocks = new ArrayList<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startPos.getX(), startPos.getZ()));
        final int minX = startPos.getX() - radius;
        final int maxX = startPos.getX() + radius;
        final int minY = startPos.getZ() - radius;
        final int maxY = startPos.getZ() + radius;

        while (!queue.isEmpty()) {

            Point p = queue.remove();

            //if inside of square
            if ((p.x >= minX) && (p.x <= maxX) && (p.y >= minY) && (p.y <= maxY)) {

                BlockPos current = new BlockPos(p.x, startPos.getY(), p.y);
                //if current block is can be used to plant the crop
                if (isTargetBlock(world, current, target) && isAir(world, current.above()) && !targetBlocks.contains(current)) {
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
    private boolean playerHasOneSeed(PlayerEntity player, Item seed) {
        return player.inventory.hasAnyOf(ImmutableSet.of(seed));
    }

    /**
     * @param player
     * @param seed
     */
    private void removeOneSeedFromPlayer(PlayerEntity player, Item seed) {
        //don't shrink player inv when in creative
        if (player.isCreative()) {
            return;
        }
        int slot = player.inventory.findSlotMatchingUnusedItem(new ItemStack(seed));
        player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(handler -> {
                    handler.extractItem(slot, 1, false);
                });
    }

    /**
     * if block at pos is considered air
     *
     * @param world
     * @param pos
     * @return
     */
    private boolean isAir(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir(world, pos);
    }

    /**
     * compare the block at pos is equal to the provided targetblock
     *
     * @param world
     * @param pos
     * @param target
     * @return
     */
    private boolean isTargetBlock(ServerWorld world, BlockPos pos, Block target) {
        return world.getBlockState(pos).getBlock().equals(target);
    }

    /**
     * if the item can be used to place crops or stems
     *
     * @param item
     * @return
     */
    private boolean isSeedItemForCrop(Item item) {
        if (!(item instanceof BlockNamedItem)) {
            return false;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(((BlockNamedItem) item).getBlock().getRegistryName());
        return ((block instanceof CropsBlock) || (block instanceof StemBlock));
    }
}
