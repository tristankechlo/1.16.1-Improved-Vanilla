package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.*;

public class CropRightClickHandler {

    private static final float BASE_MULTIPLIER = 1.0F;

    @SubscribeEvent
    public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final World level = event.getWorld();
        final EntityPlayer player = event.getEntityPlayer();
        final BlockPos targetPos = event.getPos();
        if (player == null || level == null) {
            return;
        }
        if (level.isRemote || player.isSneaking() || player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        if (!ImprovedVanillaConfig.FARMING.activated.get()) {
            return;
        }
        final Block targetBlock = level.getBlockState(targetPos).getBlock();
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getHeldItemMainhand().isEmpty()) {
            boolean success = spawnDropsAndResetBlock(level, targetPos, BASE_MULTIPLIER);
            if (success) {
                event.setCanceled(true);
                player.swingArm(event.getHand());
            }
        } else if (heldItem instanceof ItemHoe) {
            if (!ImprovedVanillaConfig.FARMING.allowHoeUsageAsLootModifier.get()) {
                return;
            }
            float multiplier = getLootMultiplier((ItemHoe) heldItem);
            boolean success = spawnDropsAndResetBlock(level, targetPos, multiplier);
            if (success) {
                event.setCanceled(true);
                player.swingArm(event.getHand());
            }
        }
    }

    private static PropertyInteger getAgeProperty(Block targetBlock) {
        if (targetBlock instanceof BlockCrops) {
            Class<? extends Block> clazz = targetBlock.getClass();
            PropertyInteger age = null;
            try {
                Method retrieveItems = clazz.getDeclaredMethod("func_185524_e");
                retrieveItems.setAccessible(true);
                age = (PropertyInteger) retrieveItems.invoke(targetBlock);
            } catch (NoSuchMethodException e) {
                try {
                    Method retrieveItems = clazz.getSuperclass().getDeclaredMethod("func_185524_e");
                    retrieveItems.setAccessible(true);
                    age = (PropertyInteger) retrieveItems.invoke(targetBlock);
                } catch (Exception e1) {
                    ImprovedVanilla.LOGGER.error("Could not find method 'getAgeProperty' from super");
                    ImprovedVanilla.LOGGER.error(e.getMessage());
                }
            } catch (Exception e) {
                ImprovedVanilla.LOGGER.error("Could not find method 'getAgeProperty'");
                ImprovedVanilla.LOGGER.error(e.getMessage());
            }
            return age;
        } else if (targetBlock.equals(Blocks.COCOA)) {
            return BlockCocoa.AGE;
        } else if (targetBlock.equals(Blocks.NETHER_WART)) {
            return BlockNetherWart.AGE;
        } else {
            return null;
        }
    }

    private static float getLootMultiplier(ItemHoe item) {
        int tierLevel = Item.ToolMaterial.valueOf(item.getMaterialName()).getHarvestLevel();
        return BASE_MULTIPLIER + (tierLevel * 0.55F);
    }

    private boolean spawnDropsAndResetBlock(World world, BlockPos pos, float multiplier) {
        //get age property
        final Block targetBlock = world.getBlockState(pos).getBlock();
        PropertyInteger ageProperty = getAgeProperty(targetBlock);
        if (ageProperty == null) {
            return false;
        }

        //check if crop is fully grown
        final IBlockState blockState = world.getBlockState(pos);
        final int maximumAge = Collections.max(ageProperty.getAllowedValues());
        final int currentAge = blockState.getValue(ageProperty);
        if (currentAge < maximumAge) {
            return false;
        }

        //reset the crop age
        final IBlockState newState = blockState.withProperty(ageProperty, 0);
        world.setBlockState(pos, newState, 3);

        //get and modify loot
        final NonNullList<ItemStack> oldLoot = NonNullList.create();
        world.getBlockState(pos).getBlock().getDrops(oldLoot, world, pos, blockState, 0);
        List<ItemStack> newLoot = getLootModified(oldLoot, multiplier);
        newLoot.forEach(stack -> Block.spawnAsEntity(world, pos, stack));
        return true;
    }

    @SuppressWarnings("deprecation")
    private static List<ItemStack> getLootModified(List<ItemStack> loot, float multiplier) {
        Map<Item, Integer> lootMap = new HashMap<>();
        loot.forEach(stack -> {
            Item item = stack.getItem();
            int amount = lootMap.getOrDefault(item, 0);
            lootMap.put(item, amount + stack.getCount());
        });

        // if the blacklist is enabled, remove items from the loot
        if (ImprovedVanillaConfig.FARMING.blacklistEnabled.get()) {
            Set<Item> itemsToRemove = ImprovedVanillaConfig.FARMING.blacklistedDrops.get();
            lootMap.keySet().removeIf(itemsToRemove::contains);
        }

        List<ItemStack> newLoot = new ArrayList<>();
        lootMap.forEach((item, amount) -> {
            int newAmount = Math.round(amount * multiplier);
            if (newAmount <= 0) {
                return;
            }
            while (newAmount > item.getItemStackLimit()) {
                newLoot.add(new ItemStack(item, item.getItemStackLimit()));
                newAmount -= item.getItemStackLimit();
            }
            newLoot.add(new ItemStack(item, newAmount));
        });

        return newLoot;
    }

}