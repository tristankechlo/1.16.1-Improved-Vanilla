package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Collections;
import java.util.List;

public final class CropRightClickHandler {

    public static InteractionResult harvestOnRightClick(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player == null || level == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide || player.isShiftKeyDown() || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        //TODO if (ImprovedVanillaConfig.SERVER.enableRightClickCrops.get() == false) {
        //    return;
        //}
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        BlockPos target = hitResult.getBlockPos();
        final Block targetBlock = level.getBlockState(target).getBlock();
        IntegerProperty age;
        if (targetBlock instanceof CropBlock) {
            age = ((CropBlock) targetBlock).getAgeProperty();
        } else if (targetBlock.equals(Blocks.COCOA)) {
            age = CocoaBlock.AGE;
        } else if (targetBlock.equals(Blocks.NETHER_WART)) {
            age = NetherWartBlock.AGE;
        } else {
            return InteractionResult.PASS;
        }
        if (spawnDropsAndResetBlock(level, target, age)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean spawnDropsAndResetBlock(final Level level, final BlockPos pos, final IntegerProperty property) {
        final BlockState blockState = level.getBlockState(pos);
        final int maximumAge = Collections.max(property.getPossibleValues());
        final int currentAge = blockState.getValue(property);
        if (currentAge < maximumAge) {
            return false;
        }
        final BlockState newState = blockState.setValue(property, 0);
        level.setBlock(pos, newState, 3);
        final List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) level, pos, null);
        drops.forEach(stack -> Block.popResource(level, pos, stack));
        return true;
    }

}
