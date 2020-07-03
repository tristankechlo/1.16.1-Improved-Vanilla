package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.block.BlockState;
import java.util.Collection;
import java.util.Collections;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraft.state.Property;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CropRightClickHandler
{
    @SubscribeEvent
    public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final PlayerEntity player = event.getPlayer();
        final BlockPos pos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (world.isRemote || player.isCreative() || player.isSneaking() || player.isSpectator()) {
            return;
        }
        if (player.getHeldItemMainhand().getItem() != Items.AIR) {
            return;
        }
        final Block targetBlock = world.getBlockState(pos).getBlock();
        Block block;
        IntegerProperty age;
        if (targetBlock instanceof CropsBlock) {
            block = targetBlock;
            if (targetBlock instanceof BeetrootBlock) {
                age = BeetrootBlock.BEETROOT_AGE;
            }
            else {
                age = CropsBlock.AGE;
            }
        }
        else {
            if (!(targetBlock instanceof CocoaBlock)) {
                return;
            }
            block = targetBlock;
            age = CocoaBlock.AGE;
        }
        if (this.spawnDropsAndResetBlock(world, pos, block, (Property<Integer>)age)) {
            player.swing(Hand.MAIN_HAND, true);
            event.setCanceled(true);
        }
    }
    
    private boolean spawnDropsAndResetBlock(final World world, final BlockPos pos, final Block block, final Property<Integer> property) {
        final BlockState blockState = world.getBlockState(pos);
        if (blockState == null) {
            return false;
        }
        final int maximumAge = Collections.max((Collection<? extends Integer>)property.getAllowedValues());
        final int currentAge = (int)blockState.get(property);
        if (currentAge < maximumAge) {
            return false;
        }
        final BlockState newState = (BlockState)blockState.with(property, 0);
        world.setBlockState(pos, newState, 3);
        final List<ItemStack> drops = (List<ItemStack>)Block.getDrops(blockState, (ServerWorld)world, pos, (TileEntity)null);
        drops.forEach(stack -> Block.spawnAsEntity(world, pos, stack));
        return true;
    }
}