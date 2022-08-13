package com.tristankechlo.improvedvanilla.config.categories;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.values.BooleanValue;
import com.tristankechlo.improvedvanilla.config.values.IntegerValue;

public final class SpawnerConfig {

    private static final String IDENTIFIER = "spawner";

    public final BooleanValue clearSpawner = new BooleanValue("clear_spawner_when_placed", true);
    public final IntegerValue spawnerDropChance = new IntegerValue("spawner_drop_chance", 100, 0, 100);
    public final IntegerValue spawnEggDropChance = new IntegerValue("spawn_egg_drop_chance", 100, 0, 100);

    public void setToDefault() {
        clearSpawner.setToDefault();
        spawnerDropChance.setToDefault();
        spawnEggDropChance.setToDefault();
    }

    public void serialize(JsonObject json) {
        JsonObject spawner = new JsonObject();
        clearSpawner.serialize(spawner);
        spawnerDropChance.serialize(spawner);
        spawnEggDropChance.serialize(spawner);
        json.add(IDENTIFIER, spawner);
    }

    public void deserialize(JsonObject json) {
        if (json.has(IDENTIFIER)) {
            JsonObject spawner = json.get(IDENTIFIER).getAsJsonObject();
            clearSpawner.deserialize(spawner);
            spawnerDropChance.deserialize(spawner);
            spawnEggDropChance.deserialize(spawner);
        }
    }

}
