package com.tristankechlo.improvedvanilla.config;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.categories.EasyPlantingConfig;
import com.tristankechlo.improvedvanilla.config.categories.FarmingConfig;
import com.tristankechlo.improvedvanilla.config.categories.MobDropConfig;
import com.tristankechlo.improvedvanilla.config.categories.SpawnerConfig;

public final class ImprovedVanillaConfig {

    public static final FarmingConfig FARMING = new FarmingConfig();
    public static final EasyPlantingConfig EASY_PLANTING = new EasyPlantingConfig();
    public static final SpawnerConfig SPAWNER = new SpawnerConfig();
    public static final MobDropConfig MOB_DROP = new MobDropConfig();

    public static void setToDefault() {
        FARMING.setToDefault();
        EASY_PLANTING.setToDefault();
        SPAWNER.setToDefault();
        MOB_DROP.setToDefault();
    }

    public static JsonObject serialize(JsonObject json) {
        FARMING.serialize(json);
        EASY_PLANTING.serialize(json);
        SPAWNER.serialize(json);
        MOB_DROP.serialize(json);
        return json;
    }

    public static void deserialize(JsonObject json) {
        FARMING.deserialize(json);
        EASY_PLANTING.deserialize(json);
        SPAWNER.deserialize(json);
        MOB_DROP.deserialize(json);
    }

}
