package com.tristankechlo.improvedvanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public final class ImprovedVanilla {

    public static final String MOD_ID = "improvedvanilla";
    public static final String MOD_NAME = "Improved Vanilla";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public static ItemStack getMonsterEgg(String id, int count) {
        final ResourceLocation search = ResourceLocation.tryParse(id + "_spawn_egg");
        Item item = BuiltInRegistries.ITEM.getValue(search);
        if (item == null || item == Items.AIR) {
            LOGGER.warn("Did not find a spawn-egg for '{}', searched for '{}'", id, search);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    public static void dropItemStackInWorld(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double d0 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        double d1 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        double d2 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        ItemEntity itementity = new ItemEntity(level, d0 + pos.getX(), d1 + pos.getY(), d2 + pos.getZ(), stack);
        itementity.setDefaultPickUpDelay();
        level.addFreshEntity(itementity);
    }

}