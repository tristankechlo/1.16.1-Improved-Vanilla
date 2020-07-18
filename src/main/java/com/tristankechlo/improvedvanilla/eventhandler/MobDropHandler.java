package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class MobDropHandler {
	    
    @SubscribeEvent
    public void onMobDeath(final LivingDeathEvent event) {
    	final LivingEntity entity = event.getEntityLiving();
    	final World world = entity.world;
    	if(!world.isRemote){
        	final Entity source = event.getSource().getTrueSource();
        	if(source instanceof ServerPlayerEntity) {
        		//final ServerPlayerEntity player = (ServerPlayerEntity)entity;

                final int dropchance = ImprovedVanillaConfig.SERVER.mobSpawnEggDropChance.get();
                if(dropchance >= 1 && dropchance <= 100) {
                    if (Math.random() < ((double)dropchance / 100)) {
                    	
                        final EntityType<?> type = (EntityType<?>)entity.getType();
                        final ItemStack stack = new ItemStack((IItemProvider)ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg")), 1);
                		entity.entityDropItem(stack);
                		
                    }
                }
        	}   		
    	}
    }
    
}