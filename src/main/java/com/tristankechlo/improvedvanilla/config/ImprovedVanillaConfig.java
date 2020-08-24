package com.tristankechlo.improvedvanilla.config;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber
public class ImprovedVanillaConfig {
	
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Server SERVER = new Server(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();
    
	public static class Server {

		public final BooleanValue enableRightClickCrops;

		public final IntValue spawnerDropChance;
		public final IntValue spawnEggDropChanceOnSpawnerDestroyed;
		
		public final BooleanValue dropOnlyWhenKilledByPlayer;
		public final BooleanValue lootingAffective;
		public final IntValue mobSpawnEggDropChance;
		
		Server(ForgeConfigSpec.Builder builder){
            builder.comment("general configuration settings")
                   .push("General");
            
            enableRightClickCrops = builder
            		.comment("If set to true, Crops can be collected by rightclicking them")
            		.define("enableRightClickCrops", true);
            
            builder.pop();
            
            
            
            builder.comment("Spawner related settings (not affective when mod \"SpawnerSettings\" is present)")
            	.push("Spawner");

			spawnerDropChance = builder.comment(
					"Drop-chance for the spawner to drop itself when mined with a silk-touch pickaxe (default 100, 100 -> always, 0 -> never)")
					.defineInRange("spawnerDropChance", 100, 0, 100);

			spawnEggDropChanceOnSpawnerDestroyed = builder
					.comment("Drop-chance for each stack, in a spawner, in % (default 100, 100 -> always, 0 -> never)")
					.defineInRange("spawnEggDropChanceOnSpawnerDestroyed", 100, 0, 100);
            
            builder.pop();
            
            
            
            builder.comment("")
            	.push("Mob-Drops");
            
            dropOnlyWhenKilledByPlayer = builder
            		.comment("If set to true, SpawnEggs only drop when the mob was killed by a player")
            		.define("dropOnlyWhenKilledByPlayer", true);
            
            lootingAffective = builder
            		.comment("If set to true, then foreach looting level on the players tool, there will by another possibility to drop the egg")
            		.define("lootingAffective", true);
            
            mobSpawnEggDropChance = builder
            		.comment("Drop-chance for all mobs to drop their spawn-egg in % (default 2, 100 -> always, 0 -> never)")
            		.defineInRange("mobSpawnEggDropChance", 2, 0, 100);
            
            builder.pop();
		}
	}

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ImprovedVanilla.LOGGER.debug("Loaded config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
    	ImprovedVanilla.LOGGER.debug("Config just got changed on the file system!");
    }
	
}
