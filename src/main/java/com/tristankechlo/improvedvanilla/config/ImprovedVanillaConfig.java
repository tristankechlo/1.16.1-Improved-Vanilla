package com.tristankechlo.improvedvanilla.config;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.categories.EasyPlantingConfig;
import com.tristankechlo.improvedvanilla.config.categories.CropRightClickConfig;
import com.tristankechlo.improvedvanilla.config.categories.MobDropConfig;
import com.tristankechlo.improvedvanilla.config.categories.SpawnerConfig;

public final class ImprovedVanillaConfig {

	public static final EasyPlantingConfig EASY_PLANTING = new EasyPlantingConfig();
	public static final CropRightClickConfig CROP_RIGHT_CLICKING = new CropRightClickConfig();
	public static final MobDropConfig MOB_DROP = new MobDropConfig();
	public static final SpawnerConfig SPAWNER = new SpawnerConfig();

	public static void setToDefault() {
		CROP_RIGHT_CLICKING.setToDefault();
		EASY_PLANTING.setToDefault();
		SPAWNER.setToDefault();
		MOB_DROP.setToDefault();
	}

	public static JsonObject serialize(JsonObject json) {
		CROP_RIGHT_CLICKING.serialize(json);
		EASY_PLANTING.serialize(json);
		SPAWNER.serialize(json);
		MOB_DROP.serialize(json);
		return json;
	}

	public static void deserialize(JsonObject json) {
		CROP_RIGHT_CLICKING.deserialize(json);
		EASY_PLANTING.deserialize(json);
		SPAWNER.deserialize(json);
		MOB_DROP.deserialize(json);
	}

}
