package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class MobDropHandler {

	@SubscribeEvent
	public void onMobDeath(final LivingDeathEvent event) {
		final LivingEntity entity = event.getEntityLiving();
		final World world = entity.world;
		if (!world.isRemote) {

			final boolean onlyWhenKilledByPlayer = ImprovedVanillaConfig.SERVER.dropOnlyWhenKilledByPlayer.get();
			final int dropchance = ImprovedVanillaConfig.SERVER.mobSpawnEggDropChance.get();
			final Entity source = event.getSource().getTrueSource();
			final EntityType<?> type = (EntityType<?>) entity.getType();
			final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg"));
			
			//killed by player and onlyWhenKilledByPlayer is true
			if ((source instanceof ServerPlayerEntity) && onlyWhenKilledByPlayer) {
				//drop only when entity was killed by a player
				final ServerPlayerEntity player = (ServerPlayerEntity) source;
				if (player.isSpectator()) {
					return;
				}

				final int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, player.getHeldItemMainhand());
				final boolean lootingAffective = ImprovedVanillaConfig.SERVER.lootingAffective.get();

				if (dropchance >= 1 && dropchance <= 100) {
					int count = 0;
					if(lootingAffective) {
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
					if(count > 0) {
						final ItemStack stack = new ItemStack(item, count);
						ItemEntity itemEntity = new ItemEntity(world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
						itemEntity.setDefaultPickupDelay();
						world.addEntity(itemEntity);
					}
				}
				
			//if onlyWhenKilledByPlayer is false
			} else if (!onlyWhenKilledByPlayer) {
				
				//if the mob was killed by player anyway
				if (source instanceof ServerPlayerEntity) {
					
					final ServerPlayerEntity player = (ServerPlayerEntity) source;
					if (player.isSpectator()) {
						return;
					}

					final int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, player.getHeldItemMainhand());
					final boolean lootingAffective = ImprovedVanillaConfig.SERVER.lootingAffective.get();
					
					if (dropchance >= 1 && dropchance <= 100) {
						int count = 0;
						if(lootingAffective) {
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
						if(count > 0) {
							final ItemStack stack = new ItemStack(item, count);
							ItemEntity itemEntity = new ItemEntity(world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
							itemEntity.setDefaultPickupDelay();
							world.addEntity(itemEntity);
						}
					}

				//not killed by player
				} else {
					if (dropchance >= 1 && dropchance <= 100) {
						if (Math.random() < ((double) dropchance / 100)) {
							final ItemStack stack = new ItemStack(item, 1);
							ItemEntity itemEntity = new ItemEntity(world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
							itemEntity.setDefaultPickupDelay();
							world.addEntity(itemEntity);
						}
					}
				}
			} else {
				return;
			}
		}
	}
}