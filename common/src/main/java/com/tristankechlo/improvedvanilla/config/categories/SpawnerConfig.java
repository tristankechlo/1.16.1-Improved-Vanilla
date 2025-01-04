package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpawnerConfig(int spawnerDropChance, int spawnEggDropChance) {

    public static final SpawnerConfig DEFAULT = new SpawnerConfig(100, 100);
    public static final Codec<SpawnerConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("spawner_drop_chance").forGetter(SpawnerConfig::spawnerDropChance),
                    Codec.intRange(0, 100).fieldOf("spawn_egg_drop_chance").forGetter(SpawnerConfig::spawnEggDropChance)
            ).apply(instance, SpawnerConfig::new)
    );

}
