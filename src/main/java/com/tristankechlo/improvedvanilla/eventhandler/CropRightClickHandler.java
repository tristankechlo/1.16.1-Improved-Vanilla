package com.tristankechlo.improvedvanilla.eventhandler;

import java.util.Collections;
import java.util.List;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CropRightClickHandler {

	@SubscribeEvent
	public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final Level world = event.getLevel();
		final Player player = event.getEntity();
		final BlockPos pos = event.getPos();
		if (player == null || world == null) {
			return;
		}
		if (world.isClientSide || player.isShiftKeyDown() || player.isSpectator()
				|| event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		if (ImprovedVanillaConfig.SERVER.enableRightClickCrops.get() == false) {
			return;
		}
		if (!player.getMainHandItem().isEmpty()) {
			return;
		}
		final Block targetBlock = world.getBlockState(pos).getBlock();
		IntegerProperty age;
		if (targetBlock instanceof CropBlock) {
			age = ((CropBlock) targetBlock).getAgeProperty();
		} else if (targetBlock.equals(Blocks.COCOA)) {
			age = CocoaBlock.AGE;
		} else if (targetBlock.equals(Blocks.NETHER_WART)) {
			age = NetherWartBlock.AGE;
		} else {
			return;
		}

		if (this.spawnDropsAndResetBlock(world, pos, age)) {
			player.swing(InteractionHand.MAIN_HAND, true);
			event.setCanceled(true);
		}
	}

	private boolean spawnDropsAndResetBlock(final Level world, final BlockPos pos, final IntegerProperty property) {
		final BlockState blockState = world.getBlockState(pos);
		if (blockState == null) {
			return false;
		}
		final int maximumAge = Collections.max(property.getPossibleValues());
		final int currentAge = blockState.getValue(property);
		if (currentAge < maximumAge) {
			return false;
		}
		final BlockState newState = blockState.setValue(property, 0);
		world.setBlock(pos, newState, 3);
		final List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) world, pos, (BlockEntity) null);
		drops.forEach(stack -> Block.popResource(world, pos, stack));
		return true;
	}

}