package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class MobDropHandler {

	@SubscribeEvent
	public void onMobDeath(final LivingDropsEvent event) {
		final LivingEntity entity = event.getEntityLiving();
		final World world = entity.level;
		if (world.isClientSide) {
			return;
		}

		final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.SERVER.dropOnlyWhenKilledByPlayer.get();
		final int dropchance = ImprovedVanillaConfig.SERVER.mobSpawnEggDropChance.get();
		final Entity source = event.getSource().getEntity();
		final BlockPos pos = entity.blockPosition();
		final EntityType<?> type = (EntityType<?>) entity.getType();
		final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg"));

		// killed by player and onlyWhenKilledByPlayer is true
		if ((source instanceof ServerPlayerEntity) && onlyWhenKilledByPlayer) {
			// drop only when entity was killed by a player
			final ServerPlayerEntity player = (ServerPlayerEntity) source;
			if (player.isSpectator()) {
				return;
			}
			handleKilledByPlayer(event, world, pos, item, dropchance);

		// if allowed to drop always (onlyWhenKilledByPlayer == true)
		} else if (!onlyWhenKilledByPlayer) {

			// if the mob was killed by player anyway
			if (source instanceof ServerPlayerEntity) {

				final ServerPlayerEntity player = (ServerPlayerEntity) source;
				if (player.isSpectator()) {
					return;
				}
				handleKilledByPlayer(event, world, pos, item, dropchance);

			// not killed by player
			} else {
				if (dropchance >= 1 && dropchance <= 100) {
					if (Math.random() < ((double) dropchance / 100)) {
						final ItemStack stack = new ItemStack(item, 1);
						ItemEntity itemEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), stack);
						itemEntity.setDefaultPickUpDelay();
						world.addFreshEntity(itemEntity);
					}
				}
			}
		}
	}

	private static void handleKilledByPlayer(LivingDropsEvent event, World world, BlockPos pos, Item item, int dropchance) {
		final int lootingLevel = event.getLootingLevel();
		final boolean lootingAffective = ImprovedVanillaConfig.SERVER.lootingAffective.get();

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
			ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			itemEntity.setDefaultPickUpDelay();
			world.addFreshEntity(itemEntity);
		}
	}

}