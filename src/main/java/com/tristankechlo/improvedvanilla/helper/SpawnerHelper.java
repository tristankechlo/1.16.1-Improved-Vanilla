package com.tristankechlo.improvedvanilla.helper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnerHelper {
	
	public static Inventory getInvfromSpawner (final World world, final BlockPos pos) {
		
		Inventory inv = new Inventory(9);

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

		        for(int i = 0; i < listnbt.size(); ++i) {
		        	if(i > inv.getSizeInventory()) {
		        		break;
		        	}
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
	
	public static void dropMonsterEggs(final World world, final BlockPos pos, int eggDropChance) {
		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			
			final Inventory inv = getInvfromSpawner(world, pos);
			
			if (eggDropChance < 1 || eggDropChance > 100) {
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
	
	public static void resetSpawner(final World world, final BlockPos pos) {
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

}
