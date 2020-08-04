package com.tristankechlo.improvedvanilla.init;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.container.SpawnerContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainerTypes {
	
	public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, ImprovedVanilla.MOD_ID);
	
	public static final RegistryObject<ContainerType<SpawnerContainer>> SPAWNER_CONTAINER = 
			CONTAINER_TYPES.register("spawner_container", () -> IForgeContainerType.create(SpawnerContainer::new));
	
}
