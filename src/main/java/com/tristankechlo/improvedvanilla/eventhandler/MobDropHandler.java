package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
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

        final EntityType<?> type = entityKilled.getType();
        final String typeName = Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(type)).toString();
        final ResourceLocation search = new ResourceLocation(typeName + "_spawn_egg");
        final Item item = ForgeRegistries.ITEMS.getValue(search);

        if (item == null) {
            ImprovedVanilla.LOGGER.info("Did not find a spawn-egg for '{}', searched for '{}'", typeName, search);
            return;
        }

        final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.MOB_DROP.dropOnlyWhenKilledByPlayer.get();
        final int dropChance = ImprovedVanillaConfig.MOB_DROP.mobSpawnEggDropChance.get();
        final Entity source = damageSource.getEntity();
        final Vec3d pos = entityKilled.position();

        // killed by player and onlyWhenKilledByPlayer is true
        if ((source instanceof ServerPlayerEntity) && onlyWhenKilledByPlayer) {
            // drop only when entity was killed by a player
            ServerPlayerEntity player = (ServerPlayerEntity) source;
            if (player.isSpectator()) {
                return;
            }
            handleKilledByPlayer(level, pos, item, dropChance, event.getLootingLevel());

        } else if (!onlyWhenKilledByPlayer) {

            // if the mob was killed by player anyway
            if (source instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) source;
                if (player.isSpectator()) {
                    return;
                }
                handleKilledByPlayer(level, pos, item, dropChance, event.getLootingLevel());
            } else {
                // not killed by player
                if (dropChance <= 0 || dropChance > 100) {
                    return;
                }
                if (Math.random() < ((double) dropChance / 100)) {
                    ItemStack stack = new ItemStack(item, 1);
                    ItemEntity itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), stack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    private static void handleKilledByPlayer(World level, Vec3d pos, Item item, int dropChance, int lootingLevel) {
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
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

}