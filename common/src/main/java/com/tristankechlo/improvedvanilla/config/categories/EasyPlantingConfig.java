package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EasyPlantingConfig(boolean activated, int radius, boolean makeCircle) {

    public static final EasyPlantingConfig DEFAULT = new EasyPlantingConfig(true, 3, true);
    public static final Codec<EasyPlantingConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(EasyPlantingConfig::activated),
                    Codec.intRange(1, 10).fieldOf("radius").forGetter(EasyPlantingConfig::radius),
                    Codec.BOOL.fieldOf("make_circle").forGetter(EasyPlantingConfig::makeCircle)
            ).apply(instance, EasyPlantingConfig::new)
    );

}
