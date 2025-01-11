package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.platform.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class CropRightClickHandler {

    public static InteractionResult onPlayerRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!ImprovedVanillaConfig.get().cropRightClicking().activated()) {
            return InteractionResult.PASS;
        }
        if (player == null || level == null) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        final Holder<Item> heldItem = BuiltInRegistries.ITEM.wrapAsHolder(player.getMainHandItem().getItem());
        AtomicReference<InteractionResult> result = new AtomicReference<>(InteractionResult.PASS);

        if (player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide()) {
                spawnDropsAndResetBlock(level, hitResult.getBlockPos(), 1.0F, () -> result.set(InteractionResult.SUCCESS));
            }
            player.swing(hand, true);
        } else if (heldItem.is(ItemTags.HOES)) {
            if (!level.isClientSide()) {
                float multiplier = ImprovedVanillaConfig.get().cropRightClicking().modifiers().getOrDefault(heldItem, 1.0F);
                spawnDropsAndResetBlock(level, hitResult.getBlockPos(), multiplier, () -> result.set(InteractionResult.SUCCESS));
            }
            player.swing(hand, true);
        }
        return result.get();
    }

    private static void spawnDropsAndResetBlock(Level level, BlockPos pos, float multiplier, Runnable success) {
        //get age property
        Block targetBlock = level.getBlockState(pos).getBlock();
        IntegerProperty ageProperty = getAgeProperty(targetBlock);
        if (ageProperty == null) {
            return;
        }

        //check if crop is fully grown
        BlockState blockState = level.getBlockState(pos);
        int maximumAge = Collections.max(ageProperty.getPossibleValues());
        int currentAge = blockState.getValue(ageProperty);
        if (currentAge < maximumAge) {
            return;
        }

        //reset the crop age
        BlockState newState = blockState.setValue(ageProperty, 0);
        level.setBlock(pos, newState, 3);

        //get and modify loot
        List<ItemStack> oldLoot = Block.getDrops(blockState, (ServerLevel) level, pos, null);
        List<ItemStack> newLoot = modifyLoot(oldLoot, multiplier);
        newLoot.forEach(stack -> Block.popResource(level, pos, stack));
        success.run();
    }

    private static IntegerProperty getAgeProperty(Block targetBlock) {
        if (targetBlock instanceof CropBlock) {
            return IPlatformHelper.INSTANCE.getAgeProperty((CropBlock) targetBlock);
        } else if (targetBlock.equals(Blocks.COCOA)) {
            return CocoaBlock.AGE;
        } else if (targetBlock.equals(Blocks.NETHER_WART)) {
            return NetherWartBlock.AGE;
        } else {
            return null;
        }
    }

    private static List<ItemStack> modifyLoot(List<ItemStack> loot, float multiplier) {
        final boolean blacklistActive = ImprovedVanillaConfig.get().cropRightClicking().removeListEnabled();
        List<Ingredient> blacklist = ImprovedVanillaConfig.get().cropRightClicking().removeList();

        Map<Item, Integer> lootMap = new HashMap<>();
        loot.forEach(stack -> {
            // if the blacklist is enabled, remove items from the loot
            if (blacklistActive && blacklist.stream().anyMatch(i -> i.test(stack))) {
                return;
            }
            Item item = stack.getItem();
            int amount = lootMap.getOrDefault(item, 0);
            lootMap.put(item, amount + stack.getCount());
        });

        List<ItemStack> newLoot = new ArrayList<>();
        lootMap.forEach((item, amount) -> {
            int newAmount = Math.round(amount * multiplier);
            if (newAmount <= 0) {
                return;
            }
            Consumer<ItemStack> splitter = createStackSplitter(newLoot::add);
            splitter.accept(new ItemStack(item, newAmount));
        });

        return newLoot;
    }

    private static Consumer<ItemStack> createStackSplitter(Consumer<ItemStack> consumer) {
        return (stack) -> {
            if (stack.getCount() < stack.getMaxStackSize()) {
                consumer.accept(stack);
            } else {
                int count = stack.getCount();

                while (count > 0) {
                    ItemStack copied = stack.copy();
                    copied.setCount(Math.min(stack.getMaxStackSize(), count));
                    count -= copied.getCount();
                    consumer.accept(copied);
                }
            }
        };
    }

}
