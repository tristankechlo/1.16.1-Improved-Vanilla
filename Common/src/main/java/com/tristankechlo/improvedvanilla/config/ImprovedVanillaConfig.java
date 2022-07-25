package com.tristankechlo.improvedvanilla.config;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.values.BooleanValue;
import com.tristankechlo.improvedvanilla.config.values.IntegerValue;

public final class ImprovedVanillaConfig {

    public static final BooleanValue enableRightClickCrops = new BooleanValue("enableRightClickCrops", true);
    public static final BooleanValue enableEasyPlanting = new BooleanValue("enableEasyPlanting", true);
    public static final IntegerValue easyPlantingRadius = new IntegerValue("easyPlantingRadius", 3, 1, 10);
    public static final BooleanValue easyPlantingCircle = new BooleanValue("easyPlantingCircle", true);
    public static final IntegerValue spawnerDropChance = new IntegerValue("spawnerDropChance", 100, 0, 100);
    public static final IntegerValue spawnEggDropChanceOnSpawnerDestroyed = new IntegerValue("spawnEggDropChanceOnSpawnerDestroyed", 100, 0, 100);
    public static final BooleanValue dropOnlyWhenKilledByPlayer = new BooleanValue("dropOnlyWhenKilledByPlayer", true);
    public static final BooleanValue lootingAffective = new BooleanValue("lootingAffective", true);
    public static final IntegerValue mobSpawnEggDropChance = new IntegerValue("mobSpawnEggDropChance", 2, 0, 100);

    public static void setToDefault() {
        enableRightClickCrops.setToDefault();
        enableEasyPlanting.setToDefault();
        easyPlantingRadius.setToDefault();
        easyPlantingCircle.setToDefault();
        spawnerDropChance.setToDefault();
        spawnEggDropChanceOnSpawnerDestroyed.setToDefault();
        dropOnlyWhenKilledByPlayer.setToDefault();
        lootingAffective.setToDefault();
        mobSpawnEggDropChance.setToDefault();
    }

    public static JsonObject serialize(JsonObject json) {
        JsonObject farming = new JsonObject();
        enableRightClickCrops.serialize(farming);
        enableEasyPlanting.serialize(farming);
        easyPlantingRadius.serialize(farming);
        easyPlantingCircle.serialize(farming);
        json.add("farming", farming);

        JsonObject spawner = new JsonObject();
        spawnerDropChance.serialize(spawner);
        spawnEggDropChanceOnSpawnerDestroyed.serialize(spawner);
        json.add("spawner", spawner);

        JsonObject mobDrops = new JsonObject();
        dropOnlyWhenKilledByPlayer.serialize(mobDrops);
        lootingAffective.serialize(mobDrops);
        mobSpawnEggDropChance.serialize(mobDrops);
        json.add("mob_drops", mobDrops);

        return json;
    }

    public static void deserialize(JsonObject json) {
        if (json.has("farming")) {
            JsonObject farming = json.get("farming").getAsJsonObject();
            enableRightClickCrops.deserialize(farming);
            enableEasyPlanting.deserialize(farming);
            easyPlantingRadius.deserialize(farming);
            easyPlantingCircle.deserialize(farming);
        }
        if (json.has("spawner")) {
            JsonObject spawner = json.get("spawner").getAsJsonObject();
            spawnerDropChance.deserialize(spawner);
            spawnEggDropChanceOnSpawnerDestroyed.deserialize(spawner);
        }
        if (json.has("mob_drops")) {
            JsonObject mobDrops = json.get("mob_drops").getAsJsonObject();
            dropOnlyWhenKilledByPlayer.deserialize(mobDrops);
            lootingAffective.deserialize(mobDrops);
            mobSpawnEggDropChance.deserialize(mobDrops);
        }
    }

}
