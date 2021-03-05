package com.tristankechlo.improvedvanilla.config;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ImprovedVanilla.MOD_ID, type = Type.INSTANCE, name = ImprovedVanilla.MOD_ID)
public class ImprovedVanillaConfig {

	@Name("Farming")
	@Comment("farming related stuff")
	public static Farming FARMING = new Farming();

	@Name("Spawner")
	@Comment("Spawner related settings")
	public static Spawner SPAWNER = new Spawner();

	@Name("Mob-Drops")
	@Comment("Mob-Drop related settings")
	public static Drops DROPS = new Drops();

	public static class Farming {

		@Comment("If set to true, Crops can be collected by rightclicking them")
		public boolean enableRightClickCrops = true;

		@Comment("If set to true, seeds will be planted in a radius when right clicked on a farm land")
		public boolean enableEasyPlanting = true;

		@RangeInt(min = 0)
		@Comment("the radius in which the seeds will be placed")
		public int easyPlantingRadius = 4;

		@Comment("if set to true, the seeds will be planted in a circle instead of a square")
		public boolean easyPlantingCircle = true;
	}

	public static class Spawner {

		@RangeInt(min = 0, max = 100)
		@Comment("Drop-chance for the spawner to drop itself when mined with a silk-touch pickaxe (default 100, 100 -> always, 0 -> never)")
		public int spawnerDropChance = 100;

		@RangeInt(min = 0, max = 100)
		@Comment("Drop-chance for each stack, in a spawner, in % (default 100, 100 -> always, 0 -> never)")
		public int spawnEggDropChanceOnSpawnerDestroyed = 100;
	}

	public static class Drops {

		@Comment("If set to true, SpawnEggs only drop when the mob was killed by a player")
		public boolean dropOnlyWhenKilledByPlayer = true;

		@Comment("If set to true, then foreach looting level on the players tool, there will by another possibility to drop the egg")
		public boolean lootingAffective = true;

		@RangeInt(min = 0, max = 100)
		@Comment("Drop-chance for all mobs to drop their spawn-egg in % (default 2, 100 -> always, 0 -> never)")
		public int mobSpawnEggDropChance = 2;
	}

	@Mod.EventBusSubscriber(modid = ImprovedVanilla.MOD_ID)
	private static class EventHandler {

		@SubscribeEvent
		public static void onConfigChangedEvent(OnConfigChangedEvent event) {
			ImprovedVanilla.LOGGER.debug(event.getModID());
			if (event.getModID().equalsIgnoreCase(ImprovedVanilla.MOD_ID)) {
				ConfigManager.sync(ImprovedVanilla.MOD_ID, Type.INSTANCE);
			}
		}
	}

}
