package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraftforge.common.ToolType;
import net.minecraft.block.Blocks;
import net.minecraftforge.event.world.BlockEvent;

public class SpawnerHandler {
	
    @SubscribeEvent
    public void onBlockBreackEvent(final BlockEvent.BreakEvent event) {
        final PlayerEntity player = event.getPlayer();
        final Block targetBlock = event.getState().getBlock();
        final World world = (World) event.getWorld();

        if (targetBlock == Blocks.SPAWNER) {
            if (player.getHeldItemMainhand().getToolTypes().contains(ToolType.PICKAXE)) {
                if (!player.isCreative() && !player.isSpectator()) {
                    final int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
                    final int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());
                    if (silkTouchLevel >= 1) {
                        event.setExpToDrop(0);
                        
                        final int spawnerDropChance = ImprovedVanillaConfig.SERVER.spawnerDropChance.get();
                        if(spawnerDropChance >= 1 && spawnerDropChance <= 100) {
                            if (Math.random() < ((double)spawnerDropChance / 100) ) {
                                final ItemStack stack = new ItemStack(Items.SPAWNER, 1);
                                final ItemEntity entity = new ItemEntity(world, (double)event.getPos().getX(), (double)event.getPos().getY(), (double)event.getPos().getZ(), stack);
                                world.addEntity((Entity)entity);
                            }
                        }
                        
                        final int eggDropChance = ImprovedVanillaConfig.SERVER.spawnEggDropChanceOnSpawnerDestroyed.get();
                        if(eggDropChance >= 1 && eggDropChance <= 100) {
                            if (Math.random() < ((double)eggDropChance / 100) ) {
                                this.dropMonsterEgg(event.getPos(), world);
                            }
                        }
                        
                    }
                    else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
                        int exp = event.getExpToDrop();
                        exp += fortuneLevel * 10;
                        event.setExpToDrop(exp);
                    }
                }
            }
            else {
                event.setExpToDrop(0);
            }
        }        
    }
    
    private void dropMonsterEgg(final BlockPos pos, final World world) {
        final MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(pos);
        final AbstractSpawner logic = spawner.getSpawnerBaseLogic();
        CompoundNBT nbt = new CompoundNBT();
        nbt = logic.write(nbt);
        String entity_string = nbt.getCompound("SpawnData").toString();
        entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
        entity_string = entity_string.substring(0, entity_string.indexOf("\""));
        if (entity_string.equalsIgnoreCase(EntityType.PIG.getRegistryName().toString())) {
            return;
        }
        final ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")), 1);
        final ItemEntity entityItem = new ItemEntity(world, (double)pos.getX(), (double)(pos.getY() + 1.0f), (double)pos.getZ(), itemStack);
        world.addEntity((Entity)entityItem);
    }
}