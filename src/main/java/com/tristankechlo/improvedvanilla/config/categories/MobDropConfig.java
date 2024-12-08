package com.tristankechlo.improvedvanilla.config.categories;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.values.BooleanValue;
import com.tristankechlo.improvedvanilla.config.values.IntegerValue;

public final class MobDropConfig {

    private static final String IDENTIFIER = "mob_drops";

    public final BooleanValue dropOnlyWhenKilledByPlayer = new BooleanValue("drop_only_when_killed_by_player", true);
    public final BooleanValue lootingAffective = new BooleanValue("looting_effective", true);
    public final IntegerValue mobSpawnEggDropChance = new IntegerValue("spawn_egg_drop_chance", 2, 0, 100);

    public void setToDefault() {
        dropOnlyWhenKilledByPlayer.setToDefault();
        lootingAffective.setToDefault();
        mobSpawnEggDropChance.setToDefault();
    }

    public void serialize(JsonObject json) {
        JsonObject mobDrop = new JsonObject();
        dropOnlyWhenKilledByPlayer.serialize(mobDrop);
        lootingAffective.serialize(mobDrop);
        mobSpawnEggDropChance.serialize(mobDrop);
        json.add(IDENTIFIER, mobDrop);
    }

    public void deserialize(JsonObject json) {
        if (json.has(IDENTIFIER)) {
            JsonObject mobDrop = json.get(IDENTIFIER).getAsJsonObject();
            dropOnlyWhenKilledByPlayer.deserialize(mobDrop);
            lootingAffective.deserialize(mobDrop);
            mobSpawnEggDropChance.deserialize(mobDrop);
        }
    }

}
