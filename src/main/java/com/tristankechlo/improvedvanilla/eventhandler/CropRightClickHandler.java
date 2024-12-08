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
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        final World world = event.getWorld();
        final EntityPlayer player = event.getEntityPlayer();
        final BlockPos targetPos = event.getPos();
        if (player == null || world == null) {
            return;
        }
        if (player.isSneaking() || player.isSpectator() || event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        if (!ImprovedVanillaConfig.CROP_RIGHT_CLICKING.activated.get()) {
            return;
        }
        final Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getHeldItemMainhand().isEmpty()) {
            if (!world.isRemote) {
                spawnDropsAndResetBlock(world, targetPos, BASE_MULTIPLIER, () -> event.setCanceled(true));
            }
            player.swingArm(EnumHand.MAIN_HAND);
        } else if (heldItem instanceof ItemHoe) {
            if (!ImprovedVanillaConfig.CROP_RIGHT_CLICKING.allowHoeUsageAsLootModifier.get()) {
                return;
            }
            float multiplier = getLootMultiplier((ItemHoe) heldItem);
            if (!world.isRemote) {
                spawnDropsAndResetBlock(world, targetPos, multiplier, () -> event.setCanceled(true));
            }
            player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    private static float getLootMultiplier(ItemHoe item) {
        int tierLevel = Item.ToolMaterial.valueOf(item.getMaterialName()).getHarvestLevel();
        return BASE_MULTIPLIER + (tierLevel * 0.55F);
    }

    private static void spawnDropsAndResetBlock(World world, BlockPos pos, float multiplier, Runnable success) {
        //get age property
        Block targetBlock = world.getBlockState(pos).getBlock();
        PropertyInteger ageProperty = getAgeProperty(targetBlock);
        if (ageProperty == null) {
            return;
        }

        //check if crop is fully grown
        IBlockState blockState = world.getBlockState(pos);
        int maximumAge = Collections.max(ageProperty.getAllowedValues());
        int currentAge = blockState.getValue(ageProperty);
        if (currentAge < maximumAge) {
            return;
        }

        //reset the crop age
        IBlockState newState = blockState.withProperty(ageProperty, 0);
        world.setBlockState(pos, newState, 3);

        //get and modify loot
        NonNullList<ItemStack> oldLoot = NonNullList.create();
        world.getBlockState(pos).getBlock().getDrops(oldLoot, world, pos, blockState, 0);
        List<ItemStack> newLoot = getLootModified(oldLoot, multiplier);
        newLoot.forEach(stack -> Block.spawnAsEntity(world, pos, stack));
        success.run();
    }

    private static PropertyInteger getAgeProperty(Block targetBlock) {
        if (targetBlock instanceof BlockCrops) {
            Class<? extends Block> clazz = targetBlock.getClass();
            PropertyInteger age = null;
            // check for possible names of the function (obfuscated and de-obfuscated)
            String[] methods = {"func_185524_e", "getAgeProperty"};
            for (String method : methods) {
                try {
                    Method retrieveItems = clazz.getDeclaredMethod(method);
                    retrieveItems.setAccessible(true);
                    age = (PropertyInteger) retrieveItems.invoke(targetBlock);
                } catch (Exception e) {
                    try {
                        Method retrieveItems = clazz.getSuperclass().getDeclaredMethod(method);
                        retrieveItems.setAccessible(true);
                        age = (PropertyInteger) retrieveItems.invoke(targetBlock);
                    } catch (Exception ignored) {
                    }
                }
                if (age != null) {
                    break;
                }
            }
            if (age == null) {
                ImprovedVanilla.LOGGER.error("Did not find correct age property on block '{}'", targetBlock);
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

    @SuppressWarnings("deprecation")
    private static List<ItemStack> getLootModified(List<ItemStack> loot, float multiplier) {
        Map<Item, Integer> lootMap = new HashMap<>();
        loot.forEach(stack -> {
            Item item = stack.getItem();
            int amount = lootMap.getOrDefault(item, 0);
            lootMap.put(item, amount + stack.getCount());
        });

        // if the blacklist is enabled, remove items from the loot
        if (ImprovedVanillaConfig.CROP_RIGHT_CLICKING.blacklistEnabled.get()) {
            Set<Item> itemsToRemove = ImprovedVanillaConfig.CROP_RIGHT_CLICKING.blacklistedDrops.get();
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
