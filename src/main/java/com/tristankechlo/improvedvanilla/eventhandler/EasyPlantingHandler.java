package com.tristankechlo.improvedvanilla.eventhandler;

import com.google.common.collect.ImmutableList;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EasyPlantingHandler {

    // for easier access, all vanilla crops
    private static final List<Item> VANILLA_SEEDS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO);

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final EntityPlayer player = event.getEntityPlayer();
        final BlockPos pos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (world.isRemote || player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        if (!ImprovedVanillaConfig.EASY_PLANTING.activated.get()) {
            return;
        }

        final Block targetBlock = world.getBlockState(pos).getBlock();
        final Item item = player.getHeldItemMainhand().getItem();
        final int radius = ImprovedVanillaConfig.EASY_PLANTING.radius.get();

        if (radius <= 0 || (!(item instanceof ItemSeeds) && !(item instanceof ItemSeedFood))) {
            return;
        }

        if (VANILLA_SEEDS.contains(item) && (targetBlock instanceof BlockFarmland) && (getPlantType(world, pos, item) == EnumPlantType.Crop)) {
            setCropsInRadius(radius, pos, Blocks.FARMLAND, (WorldServer) world, player);
            event.setCanceled(true);
        } else if ((targetBlock instanceof BlockSoulSand) && (getPlantType(world, pos, item) == EnumPlantType.Nether)) {
            setCropsInRadius(radius, pos, Blocks.SOUL_SAND, (WorldServer) world, player);
            event.setCanceled(true);
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

    private static void setCropsInRadius(int radius, BlockPos startPos, Block target, WorldServer world, EntityPlayer player) {

        List<BlockPos> targetBlocks = getTargetBlocks(radius, world, startPos, target);
        Item seedItem = player.getHeldItemMainhand().getItem();
        final boolean makeCircle = ImprovedVanillaConfig.EASY_PLANTING.makeCircle.get();
        boolean playPlantingSound = false;

        for (BlockPos pos : targetBlocks) {
            // if config is set to circle and block is not inside the circle, skip this block
            if (makeCircle && !isWithInCircleDistance(startPos, pos, radius)) {
                continue;
            }
            // if player has seeds -> plant the seeds
            if (playerHasOneSeed(player, seedItem)) {

                world.setBlockState(pos.up(), getPlant(world, startPos, seedItem));
                removeOneSeedFromPlayer(player, seedItem);
                // CHECK: award statistic
                // play sound when atleast one seed was planted
                playPlantingSound = true;
            }
        }

        // play the planting sounds
        if (playPlantingSound) {
            world.playSound(null, startPos.getX(), startPos.getY(), startPos.getZ(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    /* get all blocks in a radius using flood-fill */
    private static List<BlockPos> getTargetBlocks(int radius, WorldServer world, BlockPos startPos, Block target) {
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

    /* wether or not the endpos is the radius for the startpos */
    private static boolean isWithInCircleDistance(BlockPos start, BlockPos end, int radius) {
        double x = Math.sqrt(Math.pow((start.getX() - end.getX()), 2) + Math.pow((start.getZ() - end.getZ()), 2));
        return x <= (radius + 0.5);
    }

    /* wether or not the player has atleast one specified seed item */
    private static boolean playerHasOneSeed(EntityPlayer player, Item seed) {
        return player.inventory.hasItemStack(new ItemStack(seed));
    }

    private static void removeOneSeedFromPlayer(EntityPlayer player, Item seed) {
        // don't shrink player inv when in creative
        if (player.isCreative()) {
            return;
        }
        int slot = player.inventory.findSlotMatchingUnusedItem(new ItemStack(seed));
        player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(slot, 1, false);
    }

    /* if block at pos is considered air */
    private static boolean isAir(WorldServer world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().equals(Blocks.AIR);
    }

    /* compare the block at pos is equal to the provided targetblock */
    private static boolean isTargetBlock(WorldServer world, BlockPos pos, Block target) {
        return world.getBlockState(pos).getBlock().equals(target);
    }

}
