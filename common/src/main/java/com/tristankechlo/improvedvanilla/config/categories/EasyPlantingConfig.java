package com.tristankechlo.improvedvanilla.config.categories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record EasyPlantingConfig(boolean activated, int radius, PlacingPattern placingPattern) {

    public static final EasyPlantingConfig DEFAULT = new EasyPlantingConfig(true, 3, PlacingPattern.CIRCLE);
    public static final Codec<EasyPlantingConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(EasyPlantingConfig::activated),
                    Codec.intRange(1, 10).fieldOf("radius").forGetter(EasyPlantingConfig::radius),
                    PlacingPattern.CODEC.fieldOf("placing_pattern").forGetter(EasyPlantingConfig::placingPattern)
            ).apply(instance, EasyPlantingConfig::new)
    );

    public enum PlacingPattern implements StringRepresentable {

        CIRCLE,
        SQUARE;

        public static final Codec<PlacingPattern> CODEC = StringRepresentable.fromEnum(PlacingPattern::values);

        @Override
        public String getSerializedName() {
            return name().toUpperCase();
        }
    }

}
