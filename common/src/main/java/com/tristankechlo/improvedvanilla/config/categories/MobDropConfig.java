package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MobDropConfig(boolean dropOnlyWhenKilledByPlayer, boolean lootingAffective, int mobSpawnEggDropChance) {

    public static final MobDropConfig DEFAULT = new MobDropConfig(true, true, 2);
    public static final Codec<MobDropConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("drop_only_when_killed_by_player").forGetter(MobDropConfig::dropOnlyWhenKilledByPlayer),
                    Codec.BOOL.fieldOf("looting_effective").forGetter(MobDropConfig::lootingAffective),
                    Codec.intRange(0, 100).fieldOf("spawn_egg_drop_chance").forGetter(MobDropConfig::mobSpawnEggDropChance)
            ).apply(instance, MobDropConfig::new)
    );

}
