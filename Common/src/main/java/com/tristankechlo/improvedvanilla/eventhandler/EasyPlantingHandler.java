package com.tristankechlo.improvedvanilla.eventhandler;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class EasyPlantingHandler {

    // for easier access, all vanilla crops
    private static final List<Item> VANILLA_SEEDS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO);

    public static InteractionResult placeCropsInCircle(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player == null || level == null) {
            return InteractionResult.PASS;
        }
        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (ImprovedVanillaConfig.enableEasyPlanting.get() == false) {
            return InteractionResult.PASS;
        }

        final BlockPos pos = hitResult.getBlockPos();
        final Block targetBlock = level.getBlockState(pos).getBlock();
        final Item item = player.getMainHandItem().getItem();
        final int radius = ImprovedVanillaConfig.easyPlantingRadius.get();

        if (radius <= 0 || !(item instanceof ItemNameBlockItem)) {
            return InteractionResult.PASS;
        }

        if ((VANILLA_SEEDS.contains(item) || isSeedItemForCrop(item)) && (targetBlock instanceof FarmBlock)) {
            if (!level.isClientSide()) {
                setCropsInRadius(radius, pos, Blocks.FARMLAND, (ServerLevel) level, (ServerPlayer) player);
            }
            return InteractionResult.SUCCESS;
        } else if ((item == Items.NETHER_WART) && (targetBlock instanceof SoulSandBlock)) {
            if (!level.isClientSide()) {
                setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (ServerLevel) level, (ServerPlayer) player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * @param radius   the radius of the circle to set the crops in
     * @param startPos the position of the crop
     * @param target   the target block to set the crops to
     * @param level    the world to set the crops in
     * @param player   the player who is planting the crops
     */
    private static void setCropsInRadius(int radius, BlockPos startPos, Block target, ServerLevel level, ServerPlayer player) {

        List<BlockPos> targetBlocks = getTargetBlocks(radius, level, startPos, target);
        final Item seedItem = player.getMainHandItem().getItem();
        final boolean makeCircle = ImprovedVanillaConfig.easyPlantingCircle.get();
        boolean playPlantingSound = false;

        for (BlockPos pos : targetBlocks) {
            // if config is set to circle and block is not inside the circle, skip this
            // block
            if (makeCircle && !isWithInCircleDistance(startPos, pos, radius)) {
                continue;
            }
            // if player has seeds -> plant the seeds
            if (playerHasOneSeed(player, seedItem)) {

                Block blockFromSeed = ((ItemNameBlockItem) seedItem).getBlock(); // get the block to place
                level.setBlockAndUpdate(pos.above(), blockFromSeed.defaultBlockState()); // set the block
                removeOneSeedFromPlayer(player, seedItem); // shrink player inv
                player.awardStat(Stats.ITEM_USED.get(seedItem)); // increase vanilla item-use-counter

                // play sound when at least one seed was planted
                playPlantingSound = true;
            }
        }

        // play the planting sounds
        if (player.getMainHandItem().getItem().equals(Items.NETHER_WART) && playPlantingSound) {
            level.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.NETHER_WART_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (playPlantingSound) {
            level.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * get all blocks in a radius using flood-fill
     *
     * @param level    the level to search in
     * @param startPos the start position to search from
     * @param target   the target block to search for
     * @param radius   the radius to search in
     * @return a list of all blocks in the radius
     */
    private static List<BlockPos> getTargetBlocks(int radius, ServerLevel level, BlockPos startPos, Block target) {
        List<BlockPos> targetBlocks = new ArrayList<>();
        Queue<Point> queue = new LinkedList<>();
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
                if (isTargetBlock(level, current, target) && isAir(level, current.above()) && !targetBlocks.contains(current)) {
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
     * whether or not the end-pos is the radius for the start-pos
     *
     * @param start  start pos
     * @param end    end pos
     * @param radius radius
     * @return boolean
     */
    private static boolean isWithInCircleDistance(BlockPos start, BlockPos end, int radius) {
        double x = Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getZ() - end.getZ()), 2));
        return x <= (radius + 0.5);
    }

    /**
     * whether or not the player has at least one specified seed item
     *
     * @param player the player
     * @param seed   the seed item
     * @return boolean
     */
    private static boolean playerHasOneSeed(ServerPlayer player, Item seed) {
        return player.getInventory().hasAnyOf(ImmutableSet.of(seed));
    }

    /**
     * @param player the player to remove the seed from
     * @param seed   the seed to remove
     */
    private static void removeOneSeedFromPlayer(ServerPlayer player, Item seed) {
        // don't shrink player inv when in creative
        if (player.isCreative()) {
            return;
        }
        int slot = player.getInventory().findSlotMatchingUnusedItem(new ItemStack(seed));
        // remove one seed from player inv
        if (slot != -1) {
            player.getInventory().removeItem(slot, 1);
        }
    }

    /**
     * if block at pos is considered air
     *
     * @param level world
     * @param pos   pos of block
     * @return boolean
     */
    private static boolean isAir(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    /**
     * compare the block at pos is equal to the provided target block
     *
     * @param level  world
     * @param pos    pos of the block
     * @param target block to compare to
     * @return boolean
     */
    private static boolean isTargetBlock(ServerLevel level, BlockPos pos, Block target) {
        return level.getBlockState(pos).is(target);
    }

    /**
     * if the item can be used to place crops or stems
     *
     * @param item item to check
     * @return boolean
     */
    private static boolean isSeedItemForCrop(Item item) {
        if (!(item instanceof ItemNameBlockItem)) {
            return false;
        }
        Block block = ((ItemNameBlockItem) item).getBlock();
        return ((block instanceof CropBlock) || (block instanceof StemBlock));
    }

}
