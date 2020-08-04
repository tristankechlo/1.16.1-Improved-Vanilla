package com.tristankechlo.improvedvanilla.container;

import com.tristankechlo.improvedvanilla.helper.SpawnerHelper;
import com.tristankechlo.improvedvanilla.init.ModContainerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

public class SpawnerContainer extends Container {

	private Inventory inventory;
	private static Tuple<World, Mutable> worldPos;
	private boolean ignore = true;

	public SpawnerContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, new Inventory(9), new Tuple<World, Mutable>(playerInv.player.world, new Mutable(0D, 0D, 0D)));
	}
	
	public SpawnerContainer(final int windowId, final PlayerInventory playerInv, Inventory inventoryIN, Tuple<World, Mutable> worldPosIN) {
		super(ModContainerTypes.SPAWNER_CONTAINER.get(), windowId);

		this.inventory = inventoryIN;
		if(!worldPosIN.getB().equals(new Mutable().setPos(0, 0, 0))) {
			SpawnerContainer.worldPos = worldPosIN;
			this.ignore = false;
		}

		for (int column = 0; column < 9; column++) {
			this.addSlot(new SpawnerSlot(this.inventory, column, 8 + (column * 18), 18));
		}

		// Main Inventory
		int startX = 8;
		int startY = 84;
		int slotSizePlus2 = 18;
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(playerInv, 9 + (row * 9) + column, startX + (column * slotSizePlus2),	startY + (row * slotSizePlus2)));
			}
		}

		// Hotbar
		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(playerInv, column, startX + (column * slotSizePlus2), 142));
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		if(this.ignore == true) {
			return;
		}
		if (!playerIn.world.isRemote && !worldPos.getB().equals(new Mutable().setPos(0, 0, 0))) {
			//set new SpawnPotentials
			final World world = worldPos.getA();
			final BlockPos pos = worldPos.getB();
			
			final MobSpawnerTileEntity spawner_tile_entity = (MobSpawnerTileEntity) world.getTileEntity(pos);
			if(spawner_tile_entity == null) {
				return;
			}
			
			final AbstractSpawner spawner_logic = spawner_tile_entity.getSpawnerBaseLogic();			
			ListNBT spawnPotentials = new ListNBT();
			
			for(int i = 0; i < 9; i++) {
				Item item = this.inventory.getStackInSlot(i).getItem();
				int weight = this.inventory.getStackInSlot(i).getCount();
				if(item == Items.AIR || weight < 1) {
					continue;
				}
				if(!(item instanceof SpawnEggItem)) {
					continue;
				}
				SpawnEggItem spawnegg = (SpawnEggItem)item;
				String entity_name = spawnegg.getType(null).getRegistryName().toString();

			    CompoundNBT entity = new CompoundNBT();
			    entity.putString("id", entity_name);
			    
			    CompoundNBT entry = new CompoundNBT();
			    entry.put("Entity", entity);
			    entry.putInt("Weight", weight);
			    
			    spawnPotentials.add(entry);
			    
			}
			
			this.inventory.clear();
			
			if(spawnPotentials.isEmpty()) {
				SpawnerHelper.resetSpawner(world, pos);
			} else {
				CompoundNBT nbt = new CompoundNBT();
				nbt = spawner_logic.write(nbt);
				nbt.put("SpawnPotentials", spawnPotentials);
				nbt.put("SpawnData", spawnPotentials.getCompound(0).getCompound("Entity"));
				spawner_logic.read(nbt);
				spawner_tile_entity.markDirty();
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);		
			}
			
			
		}
		super.onContainerClosed(playerIn);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.inventory.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < 9) {
				if (!this.mergeItemStack(itemstack1, 9, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	class SpawnerSlot extends Slot {

		public SpawnerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			Item item = stack.getItem();
			if (item instanceof SpawnEggItem) {
				return true;
			}
			return false;
		}

	}

}
