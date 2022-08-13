package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public final class CropRightClickHandler {

    private static final float BASE_MULTIPLIER = 1.0F;

    public static InteractionResult harvestOnRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player == null || level == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide || player.isShiftKeyDown() || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!ImprovedVanillaConfig.enableRightClickCrops.get()) {
            return InteractionResult.PASS;
        }

        BlockPos targetPos = hitResult.getBlockPos();
        Item heldItem = player.getMainHandItem().getItem();

        if (player.getMainHandItem().isEmpty()) {
            spawnDropsAndResetBlock(level, targetPos, BASE_MULTIPLIER);
            return InteractionResult.SUCCESS;
        } else if (heldItem instanceof HoeItem) {
            if (!ImprovedVanillaConfig.enableLootMultiplierForHoes.get()) {
                return InteractionResult.PASS;
            }
            float multiplier = getLootMultiplier((HoeItem) heldItem);
            spawnDropsAndResetBlock(level, targetPos, multiplier);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static IntegerProperty getAgeProperty(Block targetBlock) {
        if (targetBlock instanceof CropBlock) {
            return ((CropBlock) targetBlock).getAgeProperty();
        } else if (targetBlock.equals(Blocks.COCOA)) {
            return CocoaBlock.AGE;
        } else if (targetBlock.equals(Blocks.NETHER_WART)) {
            return NetherWartBlock.AGE;
        } else {
            return null;
        }
    }

    private static float getLootMultiplier(HoeItem item) {
        int tierLevel = item.getTier().getLevel();
        return BASE_MULTIPLIER + (tierLevel * 0.55F);
    }

    private static void spawnDropsAndResetBlock(Level level, BlockPos pos, float multiplier) {
        //get age property
        Block targetBlock = level.getBlockState(pos).getBlock();
        IntegerProperty ageProperty = getAgeProperty(targetBlock);
        if (ageProperty == null) {
            return;
        }

        //check if crop is fully grown
        final BlockState blockState = level.getBlockState(pos);
        final int maximumAge = Collections.max(ageProperty.getPossibleValues());
        final int currentAge = blockState.getValue(ageProperty);
        if (currentAge < maximumAge) {
            return;
        }

        //reset the crop age
        final BlockState newState = blockState.setValue(ageProperty, 0);
        level.setBlock(pos, newState, 3);

        //get and modify loot
        List<ItemStack> oldLoot = Block.getDrops(blockState, (ServerLevel) level, pos, null);
        List<ItemStack> newLoot = getLootModified(oldLoot, multiplier);
        newLoot.forEach(stack -> Block.popResource(level, pos, stack));
    }

    private static List<ItemStack> getLootModified(List<ItemStack> loot, float multiplier) {
        Map<Item, Integer> lootMap = new HashMap<>();
        loot.forEach(stack -> {
            Item item = stack.getItem();
            int amount = lootMap.getOrDefault(item, 0);
            lootMap.put(item, amount + stack.getCount());
        });

        List<ItemStack> newLoot = new ArrayList<>();
        lootMap.forEach((item, amount) -> {
            int newAmount = Math.round(amount * multiplier);
            if (newAmount <= 0) {return;}
            while (newAmount > item.getMaxStackSize()) {
                newLoot.add(new ItemStack(item, item.getMaxStackSize()));
                newAmount -= item.getMaxStackSize();
            }
            newLoot.add(new ItemStack(item, newAmount));
        });

        return newLoot;
    }

}
