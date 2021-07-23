package com.tristankechlo.improvedvanilla.eventhandler;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnerHandler {

	@SubscribeEvent
	public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
		if (ImprovedVanilla.SpawnerSettingsLoaded) {
			return;
		}
		final Level world = (Level) event.getWorld();
		final BlockPos pos = event.getPos();

		if (world.isClientSide) {
			return;
		}

		final Block targetblock = world.getBlockState(pos).getBlock();

		if (targetblock == Blocks.SPAWNER) {

			world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
			BlockEntity tileentity = world.getBlockEntity(pos);
			((SpawnerBlockEntity) tileentity).getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
			tileentity.setChanged();
			world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);

		}

	}

	@SubscribeEvent
	public void onBlockBreackEvent(final BlockEvent.BreakEvent event) {
		if (ImprovedVanilla.SpawnerSettingsLoaded) {
			return;
		}
		final Player player = event.getPlayer();
		final Block targetBlock = event.getState().getBlock();
		final Level world = (Level) event.getWorld();
		final BlockPos pos = event.getPos();

		if (world.isClientSide) {
			return;
		}

		if (targetBlock == Blocks.SPAWNER) {
			if (player.getMainHandItem().getItem() instanceof PickaxeItem) {
				if (!player.isCreative() && !player.isSpectator()) {

					final int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,
							player.getMainHandItem());
					final int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,
							player.getMainHandItem());

					if (silkTouchLevel >= 1) {

						event.setExpToDrop(0);
						final int spawnerDropChance = ImprovedVanillaConfig.SERVER.spawnerDropChance.get();

						if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
							if (Math.random() < ((double) spawnerDropChance / 100)) {
								final ItemStack stack = new ItemStack(Items.SPAWNER, 1);
								final ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(),
										stack);
								world.addFreshEntity(entity);
							}
						} else {
							int exp = event.getExpToDrop();
							exp += (exp + 1) * world.random.nextInt(4) * world.random.nextInt(4);
							event.setExpToDrop(exp);
						}

						int eggDropChance = ImprovedVanillaConfig.SERVER.spawnEggDropChanceOnSpawnerDestroyed.get();
						this.dropMonsterEggs(world, pos, eggDropChance);

						// if other mods prevent the block breack, atleast the spawner is disabled
						this.resetSpawner(world, pos);

					} else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
						int exp = event.getExpToDrop();
						exp += (exp + 1) * world.random.nextInt(fortuneLevel) * world.random.nextInt(fortuneLevel);
						event.setExpToDrop(exp);
					}
				}
			} else {
				event.setExpToDrop(0);
			}
		}
	}

	private void resetSpawner(final Level world, final BlockPos pos) {
		if (world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			world.removeBlockEntity(pos);
			world.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
			SpawnerBlockEntity tile = (SpawnerBlockEntity) world.getBlockEntity(pos);

			CompoundTag entity = new CompoundTag();
			entity.putString("id", "minecraft:area_effect_cloud");

			CompoundTag nbt = new CompoundTag();
			nbt.put("Entity", entity);
			nbt.putInt("Weight", 1);

			final SpawnData nextSpawnData = new SpawnData(nbt);
			tile.getSpawner().setNextSpawnData(world, pos, nextSpawnData);
			tile.getSpawner().setEntityId(EntityType.AREA_EFFECT_CLOUD);
			tile.setChanged();
			world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

	private void dropMonsterEggs(final Level world, final BlockPos pos, int eggDropChance) {
		if (eggDropChance <= 0) {
			return;
		}
		if (world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {

			final SimpleContainer inv = getInvfromSpawner(world, pos);

			if (eggDropChance > 100) {
				eggDropChance = 100;
			}

			for (int i = 0; i < inv.getContainerSize(); i++) {

				Item item = inv.getItem(i).getItem();
				int weight = inv.getItem(i).getCount();

				if (item == Items.AIR || weight < 1) {
					continue;
				}

				if (Math.random() < ((double) eggDropChance / 100)) {
					final ItemEntity entityItem = new ItemEntity(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(),
							inv.getItem(i));
					world.addFreshEntity(entityItem);
				}
			}
		}
	}

	private SimpleContainer getInvfromSpawner(final Level world, final BlockPos pos) {

		SimpleContainer inv = new SimpleContainer(11);

		if (world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {

			final BlockEntity tile = world.getBlockEntity(pos);
			if (!(tile instanceof SpawnerBlockEntity)) {
				return inv;
			}
			final BaseSpawner logic = ((SpawnerBlockEntity) tile).getSpawner();
			CompoundTag nbt = new CompoundTag();
			nbt = logic.save(world, pos, nbt);

			if (nbt.contains("SpawnPotentials", 9)) {
				ListTag listnbt = nbt.getList("SpawnPotentials", 10);
				int min = Math.min(11, listnbt.size());

				for (int i = 0; i < min; ++i) {
					CompoundTag entry = listnbt.getCompound(i);
					String entity = entry.getCompound("Entity").toString();
					entity = entity.substring(entity.indexOf("\"") + 1);
					entity = entity.substring(0, entity.indexOf("\""));
					int weight = entry.getShort("Weight");
					if (entity.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString())) {
						continue;
					}
					final ItemStack itemStack = new ItemStack(
							ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity + "_spawn_egg")), weight);
					inv.setItem(i, itemStack);
				}
			}
		}

		return inv;
	}
}