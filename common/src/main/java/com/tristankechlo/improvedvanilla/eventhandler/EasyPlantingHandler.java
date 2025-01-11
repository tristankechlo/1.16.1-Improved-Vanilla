package com.tristankechlo.improvedvanilla.eventhandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.config.categories.EasyPlantingConfig;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class EasyPlantingHandler {

    //for easier access, all vanilla crops
    private static final List<Item> VANILLA_SEEDS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO);

    public static InteractionResult onPlayerRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!ImprovedVanillaConfig.get().easyPlanting().activated()) {
            return InteractionResult.PASS;
        }
        final BlockPos pos = hitResult.getBlockPos();
        if (player == null || level == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        final Block targetBlock = level.getBlockState(pos).getBlock();
        final Item item = player.getMainHandItem().getItem();
        final int radius = ImprovedVanillaConfig.get().easyPlanting().radius();

        if (radius <= 0 || !(item instanceof BlockItem)) {
            return InteractionResult.PASS;
        }

        if ((VANILLA_SEEDS.contains(item) || isSeedItemForCrop(item)) && (targetBlock instanceof FarmBlock)) {
            setCropsInRadius(radius, pos, Blocks.FARMLAND, (ServerLevel) level, (ServerPlayer) player);
            return InteractionResult.SUCCESS;
        } else if ((item == Items.NETHER_WART) && (targetBlock instanceof SoulSandBlock)) {
            setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (ServerLevel) level, (ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void setCropsInRadius(int radius, BlockPos startPos, Block target, ServerLevel level, ServerPlayer player) {
        final List<BlockPos> targetBlocks = getTargetBlocks(radius, level, startPos, target);
        final ItemStack mainHandItem = player.getMainHandItem();
        final Item seedItem = player.getMainHandItem().getItem();
        final BlockState blockFromSeed = ((BlockItem) seedItem).getBlock().defaultBlockState();
        final boolean makeCircle = ImprovedVanillaConfig.get().easyPlanting().placingPattern() == EasyPlantingConfig.PlacingPattern.CIRCLE;
        boolean playPlantingSound = false;

        for (BlockPos pos : targetBlocks) {
            // if config is set to circle and block is not inside the circle, skip this block
            if (makeCircle && !isWithInCircleDistance(startPos, pos, radius)) {
                continue;
            }
            // if player has seeds -> plant the seeds
            if (playerHasOneSeed(player, seedItem)) {
                level.setBlockAndUpdate(pos.above(), blockFromSeed);
                removeOneSeedFromPlayer(player, seedItem);
                player.awardStat(Stats.ITEM_USED.get(seedItem)); // increase vanilla item-use-counter (scoreboard)
                CriteriaTriggers.PLACED_BLOCK.trigger(player, pos.above(), mainHandItem); // award player statistic for items used

                playPlantingSound = true;
            }
        }

        if (!playPlantingSound) {
            return;
        }
        // play sound when at least one seed was planted
        if (mainHandItem.is(Items.NETHER_WART)) {
            level.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.NETHER_WART_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

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

    private static boolean isWithInCircleDistance(BlockPos start, BlockPos end, int radius) {
        double x = Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getZ() - end.getZ()), 2));
        return x <= (radius + 0.5);
    }

    private static boolean playerHasOneSeed(ServerPlayer player, Item seed) {
        return player.getInventory().hasAnyOf(ImmutableSet.of(seed));
    }

    private static void removeOneSeedFromPlayer(ServerPlayer player, Item seed) {
        if (player.isCreative()) {
            return; // don't shrink player inv when in creative
        }
        int slot = player.getInventory().findSlotMatchingItem(new ItemStack(seed));
        if (slot != -1) {
            player.getInventory().removeItem(slot, 1);
        }
    }

    private static boolean isAir(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static boolean isTargetBlock(ServerLevel level, BlockPos pos, Block target) {
        return level.getBlockState(pos).is(target);
    }

    private static boolean isSeedItemForCrop(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        }
        Block block = ((BlockItem) item).getBlock();
        return ((block instanceof CropBlock) || (block instanceof StemBlock));
    }

}
