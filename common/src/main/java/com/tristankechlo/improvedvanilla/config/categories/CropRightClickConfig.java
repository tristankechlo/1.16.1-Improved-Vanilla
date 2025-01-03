package com.tristankechlo.improvedvanilla.config.categories;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public record CropRightClickConfig(
        boolean activated, boolean allowHoeUsageAsLootModifier, boolean blacklistEnabled, List<Item> blacklistedDrops
) {

    public static final CropRightClickConfig DEFAULT = new CropRightClickConfig(true, true, false, ImmutableList.of(Items.WHEAT_SEEDS));
    public static final Codec<Item> ITEM_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> BuiltInRegistries.ITEM.getOptional(id)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown item: " + id)),
            BuiltInRegistries.ITEM::getKey
    );
    public static final Codec<CropRightClickConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(CropRightClickConfig::activated),
                    Codec.BOOL.fieldOf("allow_hoe_usage_as_loot_modifier").forGetter(CropRightClickConfig::allowHoeUsageAsLootModifier),
                    Codec.BOOL.fieldOf("enable_blacklist").forGetter(CropRightClickConfig::blacklistEnabled),
                    ITEM_CODEC.listOf().fieldOf("blacklisted_drops").forGetter(CropRightClickConfig::blacklistedDrops)
            ).apply(instance, CropRightClickConfig::new)
    );

}
