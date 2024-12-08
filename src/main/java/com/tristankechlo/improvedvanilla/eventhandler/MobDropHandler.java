package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber
public class MobDropHandler {

    @SubscribeEvent
    public void onMobDeath(final LivingDropsEvent event) {
        final LivingEntity entityKilled = event.getEntityLiving();
        final DamageSource damageSource = event.getSource();
        final World level = entityKilled.level;
        if (level.isClientSide()) {
            return;
        }

        final String entityID = Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(entityKilled.getType())).toString();
        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.MOB_DROP.dropOnlyWhenKilledByPlayer.get();
        final int dropChance = ImprovedVanillaConfig.MOB_DROP.mobSpawnEggDropChance.get();
        final Entity player = damageSource.getEntity();
        final BlockPos pos = new BlockPos(entityKilled.position());

        // killed by player and onlyWhenKilledByPlayer is true
        if ((player instanceof ServerPlayerEntity) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            if (player.isSpectator()) {
                return;
            }
            handleKilledByPlayer(level, pos, dropChance, event.getLootingLevel(), entityID);

        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (player instanceof ServerPlayerEntity) {
                if (player.isSpectator()) {
                    return;
                }
                handleKilledByPlayer(level, pos, dropChance, event.getLootingLevel(), entityID);
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

    private static void handleKilledByPlayer(World level, BlockPos pos, int dropChance, int lootingLevel, String id) {
        final boolean lootingAffective = ImprovedVanillaConfig.MOB_DROP.lootingAffective.get();
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
