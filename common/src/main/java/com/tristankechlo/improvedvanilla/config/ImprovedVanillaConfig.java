package com.tristankechlo.improvedvanilla.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.categories.CropRightClickConfig;
import com.tristankechlo.improvedvanilla.config.categories.EasyPlantingConfig;
import com.tristankechlo.improvedvanilla.config.categories.MobDropConfig;
import com.tristankechlo.improvedvanilla.config.categories.SpawnerConfig;

public record ImprovedVanillaConfig(
        CropRightClickConfig cropRightClicking, EasyPlantingConfig easyPlanting, MobDropConfig mobDrop, SpawnerConfig spawner
) {

    public static final Codec<ImprovedVanillaConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    CropRightClickConfig.CODEC.fieldOf("right_click_to_harvest").forGetter(ImprovedVanillaConfig::cropRightClicking),
                    EasyPlantingConfig.CODEC.fieldOf("easy_planting").forGetter(ImprovedVanillaConfig::easyPlanting),
                    MobDropConfig.CODEC.fieldOf("mob_drops").forGetter(ImprovedVanillaConfig::mobDrop),
                    SpawnerConfig.CODEC.fieldOf("spawner").forGetter(ImprovedVanillaConfig::spawner)
            ).apply(instance, ImprovedVanillaConfig::new)
    );

    public static final ImprovedVanillaConfig DEFAULT = new ImprovedVanillaConfig(CropRightClickConfig.DEFAULT, EasyPlantingConfig.DEFAULT, MobDropConfig.DEFAULT, SpawnerConfig.DEFAULT);
    private static ImprovedVanillaConfig INSTANCE = DEFAULT;

    public static ImprovedVanillaConfig get() {
        return INSTANCE;
    }

    public static void setToDefault() {
        INSTANCE = ImprovedVanillaConfig.DEFAULT;
    }

    public static JsonElement serialize() {
        DataResult<JsonElement> result = ImprovedVanillaConfig.CODEC.encodeStart(JsonOps.INSTANCE, INSTANCE);
        result.error().ifPresent((partial) -> ImprovedVanilla.LOGGER.error(partial.message()));
        return result.result().orElseThrow();
    }

    public static void deserialize(JsonElement json) {
        DataResult<ImprovedVanillaConfig> result = ImprovedVanillaConfig.CODEC.parse(JsonOps.INSTANCE, json);
        result.error().ifPresent((partial) -> ImprovedVanilla.LOGGER.error(partial.message()));
        INSTANCE = result.result().orElseThrow();
    }

}
