package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class MobDropHandler {

    public static void onMobDeath(Level level, LivingEntity entityKilled, DamageSource damageSource) {
        if (level.isClientSide()) {
            return;
        }

        final String entityID = Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entityKilled.getType())).toString();
        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.get().mobDrop().dropOnlyWhenKilledByPlayer();
        final int dropChance = ImprovedVanillaConfig.get().mobDrop().mobSpawnEggDropChance();
        final Entity player = damageSource.getEntity();
        final BlockPos pos = entityKilled.blockPosition();

        // determine looting level
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        // killed by player and onlyWhenKilledByPlayer is true
        if ((player instanceof ServerPlayer) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            if (player.isSpectator()) {
                return;
            }
            int lootingLevel = EnchantmentHelper.getEnchantmentLevel(registry.getOrThrow(Enchantments.LOOTING), (ServerPlayer) player);
            handleKilledByPlayer(level, pos, dropChance, lootingLevel, entityID);

        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (player instanceof ServerPlayer) {
                if (player.isSpectator()) {
                    return;
                }
                int lootingLevel = EnchantmentHelper.getEnchantmentLevel(registry.getOrThrow(Enchantments.LOOTING), (ServerPlayer) player);
                handleKilledByPlayer(level, pos, dropChance, lootingLevel, entityID);
            } else {
                // not killed by player
                if (dropChance <= 0 || dropChance > 100) {
                    return;
                }
                if (Math.random() < ((double) dropChance / 100)) {
                    ItemStack stack = ImprovedVanilla.getMonsterEgg(entityID, 1);
                    ImprovedVanilla.dropItemStackInWorld(level, pos, stack);
                }
            }
        }
    }

    private static void handleKilledByPlayer(Level level, BlockPos pos, int dropChance, int lootingLevel, String id) {
        final boolean lootingAffective = ImprovedVanillaConfig.get().mobDrop().lootingAffective();
        int count = 0;
        if (lootingAffective && lootingLevel >= 1) {
            // foreach lootingLevel there's an additional chance to drop the egg
            for (int i = 0; i < (1 + lootingLevel); i++) {
                if (Math.random() < ((double) dropChance / 100)) {
                    count++;
                }
            }
        } else {
            if (Math.random() < ((double) dropChance / 100)) {
                count++;
            }
        }
        if (count > 0) {
            ItemStack stack = ImprovedVanilla.getMonsterEgg(id, count);
            ImprovedVanilla.dropItemStackInWorld(level, pos, stack);
        }
    }

}
