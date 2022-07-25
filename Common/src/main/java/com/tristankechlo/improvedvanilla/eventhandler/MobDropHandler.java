package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class MobDropHandler {

    public static void onLivingDrops(Level level, LivingEntity entityKilled, DamageSource damageSource, int lootingLevel) {
        if (level.isClientSide) {
            return;
        }
        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.dropOnlyWhenKilledByPlayer.get();
        final int dropchance = ImprovedVanillaConfig.mobSpawnEggDropChance.get();
        final Entity source = damageSource.getEntity();
        final BlockPos pos = entityKilled.blockPosition();
        final EntityType<?> type = entityKilled.getType();
        final String typeName = Registry.ENTITY_TYPE.getKey(type).toString();
        final Item item = Registry.ITEM.get(new ResourceLocation(typeName + "_spawn_egg"));

        // killed by player and onlyWhenKilledByPlayer is true
        if ((source instanceof ServerPlayer) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            final ServerPlayer player = (ServerPlayer) source;
            if (player.isSpectator()) {
                return;
            }
            handleKilledByPlayer(level, pos, item, dropchance, lootingLevel);

            // if allowed to drop always (onlyWhenKilledByPlayer == true)
        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (source instanceof ServerPlayer) {

                final ServerPlayer player = (ServerPlayer) source;
                if (player.isSpectator()) {
                    return;
                }
                handleKilledByPlayer(level, pos, item, dropchance, lootingLevel);

                // not killed by player
            } else {
                if (dropchance >= 1 && dropchance <= 100) {
                    if (Math.random() < ((double) dropchance / 100)) {
                        final ItemStack stack = new ItemStack(item, 1);
                        ItemEntity itemEntity = new ItemEntity(level, entityKilled.getX(), entityKilled.getY(), entityKilled.getZ(), stack);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }
            }
        }
    }

    private static void handleKilledByPlayer(Level level, BlockPos pos, Item item, int dropchance, int lootingLevel) {
        final boolean lootingAffective = ImprovedVanillaConfig.lootingAffective.get();

        int count = 0;
        if (lootingAffective && lootingLevel >= 1) {
            // foreach lootinglevel there's an additional chance to drop the egg
            for (int i = 0; i < (1 + lootingLevel); i++) {
                if (Math.random() < ((double) dropchance / 100)) {
                    count++;
                }
            }
        } else {
            if (Math.random() < ((double) dropchance / 100)) {
                count++;
            }
        }
        if (count > 0) {
            final ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

}
