package com.tristankechlo.improvedvanilla.eventhandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MobDropHandler {
	
    @SubscribeEvent
    public void onMobDrop(final LivingDropsEvent event) {
        final Entity entity = event.getEntity();
        final EntityType<?> type = (EntityType<?>)entity.getType();
        if (Math.random() < 0.01) {
            final ItemStack stack = new ItemStack((IItemProvider)ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg")));
            event.getDrops().add(new ItemEntity(entity.getEntityWorld(), entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack));
        }
    }
}