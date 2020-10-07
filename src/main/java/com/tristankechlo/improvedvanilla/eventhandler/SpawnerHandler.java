package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraftforge.common.ToolType;
import net.minecraft.block.Blocks;
import net.minecraftforge.event.world.BlockEvent;

public class SpawnerHandler {
	
	@SubscribeEvent
	public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
		if(ImprovedVanilla.SpawnerSettingsLoaded) {
			return;
		}
		final World world = (World) event.getWorld();
		final BlockPos pos = event.getPos();

		if (world.isRemote) {
			return;
		}
		
		final Block targetblock = world.getBlockState(pos).getBlock();
		
		if (targetblock == Blocks.SPAWNER) {
			
	    	world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
	    	TileEntity tileentity = world.getTileEntity(pos);	    	
	    	((MobSpawnerTileEntity) tileentity).getSpawnerBaseLogic().setEntityType(EntityType.AREA_EFFECT_CLOUD);
	    	tileentity.markDirty();
	    	world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
	    	
		}
		
	}

	@SubscribeEvent
	public void onBlockBreackEvent(final BlockEvent.BreakEvent event) {
		if(ImprovedVanilla.SpawnerSettingsLoaded) {
			return;
		}
		final PlayerEntity player = event.getPlayer();
		final Block targetBlock = event.getState().getBlock();
		final World world = (World) event.getWorld();
		final BlockPos pos = event.getPos();
		
		if(world.isRemote) {
			return;
		}

		if (targetBlock == Blocks.SPAWNER) {
			if (player.getHeldItemMainhand().getToolTypes().contains(ToolType.PICKAXE)) {
				if (!player.isCreative() && !player.isSpectator()) {
					
					final int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
					final int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());
					
					if (silkTouchLevel >= 1) {
						
						event.setExpToDrop(0);
						final int spawnerDropChance = ImprovedVanillaConfig.SERVER.spawnerDropChance.get();
						
						if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
							if (Math.random() < ((double) spawnerDropChance / 100)) {
								final ItemStack stack = new ItemStack(Items.SPAWNER, 1);
								final ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
								world.addEntity(entity);
							}
						} else {
							int exp = event.getExpToDrop();
							exp += (exp + 1) * world.rand.nextInt(4) * world.rand.nextInt(4);
							event.setExpToDrop(exp);
						}

						int eggDropChance = ImprovedVanillaConfig.SERVER.spawnEggDropChanceOnSpawnerDestroyed.get();
						this.dropMonsterEggs(world, pos, eggDropChance);
						
						//if other mods prevent the block breack, atleast the spawner is disabled
						this.resetSpawner(world, pos);

					} else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
						int exp = event.getExpToDrop();
						exp += (exp + 1) * world.rand.nextInt(fortuneLevel) * world.rand.nextInt(fortuneLevel);
						event.setExpToDrop(exp);
					}
				}
			} else {
				event.setExpToDrop(0);
			}
		}
	}
	

	private void resetSpawner(final World world, final BlockPos pos) {
		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			world.removeTileEntity(pos);
			world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
			MobSpawnerTileEntity tile = (MobSpawnerTileEntity) world.getTileEntity(pos);
			
			CompoundNBT entity = new CompoundNBT();
			entity.putString("id", "minecraft:area_effect_cloud");
			
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Entity", entity);
			nbt.putInt("Weight", 1);
			
			final WeightedSpawnerEntity nextSpawnData = new WeightedSpawnerEntity(nbt);
			tile.getSpawnerBaseLogic().setNextSpawnData(nextSpawnData);
			tile.getSpawnerBaseLogic().setEntityType(EntityType.AREA_EFFECT_CLOUD);
			tile.markDirty();
	    	world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}
	
	private void dropMonsterEggs(final World world, final BlockPos pos, int eggDropChance) {
		if(eggDropChance <= 0) {
			return;
		}
		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			
			final Inventory inv = getInvfromSpawner(world, pos);
						
			if (eggDropChance > 100) {
				eggDropChance = 100;
			}
			
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				
				Item item = inv.getStackInSlot(i).getItem();
				int weight = inv.getStackInSlot(i).getCount();
				
				if(item == Items.AIR || weight < 1) {
					continue;
				}

				if (Math.random() < ((double) eggDropChance / 100)) {
					final ItemEntity entityItem = new ItemEntity(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), inv.getStackInSlot(i));
					world.addEntity(entityItem);
				}
			}
		}
	}
	
	private Inventory getInvfromSpawner (final World world, final BlockPos pos) {
		
		Inventory inv = new Inventory(11);

		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			
			final TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof MobSpawnerTileEntity)) {
				return inv;
			}
			final AbstractSpawner logic = ((MobSpawnerTileEntity)tile).getSpawnerBaseLogic();
			CompoundNBT nbt = new CompoundNBT();
			nbt = logic.write(nbt);

		    if (nbt.contains("SpawnPotentials", 9)) {
		    	ListNBT listnbt = nbt.getList("SpawnPotentials", 10);
		    	int min = Math.min(11, listnbt.size());
		    	
		        for(int i = 0; i < min; ++i) {
		        	CompoundNBT entry = listnbt.getCompound(i);
		        	String entity = entry.getCompound("Entity").toString();
		        	entity = entity.substring(entity.indexOf("\"") + 1);
		    		entity = entity.substring(0, entity.indexOf("\""));
		        	int weight = entry.getShort("Weight");
		    		if (entity.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString())) {
		    			continue;
		    		}
		    		final ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity + "_spawn_egg")), weight);
		    		inv.setInventorySlotContents(i, itemStack);
		        }
		    }
		}
		
		return inv;
	}
}