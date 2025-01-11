package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MobDropConfig(
        boolean activated, boolean dropOnlyWhenKilledByPlayer, boolean lootingAffective, double dropChance
) {

    public static final MobDropConfig DEFAULT = new MobDropConfig(true, true, true, 2.0);
    public static final Codec<MobDropConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(MobDropConfig::activated),
                    Codec.BOOL.fieldOf("only_when_killed_by_player").forGetter(MobDropConfig::dropOnlyWhenKilledByPlayer),
                    Codec.BOOL.fieldOf("looting_effective").forGetter(MobDropConfig::lootingAffective),
                    Codec.doubleRange(0.0, 100.0).fieldOf("drop_chance").forGetter(MobDropConfig::dropChance)
            ).apply(instance, MobDropConfig::new)
    );

}
