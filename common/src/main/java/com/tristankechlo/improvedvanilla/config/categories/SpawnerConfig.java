package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpawnerConfig(
        boolean activated, boolean needsSilkTouch, double spawnerDropChance, double spawnEggDropChance
) {

    public static final SpawnerConfig DEFAULT = new SpawnerConfig(true, true, 100.0, 100.0);
    public static final Codec<SpawnerConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(SpawnerConfig::activated),
                    Codec.BOOL.fieldOf("needs_silktouch").forGetter(SpawnerConfig::needsSilkTouch),
                    Codec.doubleRange(0.0, 100.0).fieldOf("spawner_drop_chance").forGetter(SpawnerConfig::spawnerDropChance),
                    Codec.doubleRange(0.0, 100.0).fieldOf("spawn_egg_drop_chance").forGetter(SpawnerConfig::spawnEggDropChance)
            ).apply(instance, SpawnerConfig::new)
    );

}
