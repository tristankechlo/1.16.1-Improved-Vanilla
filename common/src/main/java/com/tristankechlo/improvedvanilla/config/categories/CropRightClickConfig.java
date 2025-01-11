package com.tristankechlo.improvedvanilla.config.categories;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CropRightClickConfig(
        boolean activated, Map<Holder<Item>, Float> modifiers, boolean removeListEnabled, List<Ingredient> removeList
) {

    public static final CropRightClickConfig DEFAULT = new CropRightClickConfig(
            true, defaultModifier(), false, ImmutableList.of(Ingredient.of(Items.WHEAT_SEEDS))
    );
    public static final Codec<CropRightClickConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(CropRightClickConfig::activated),
                    Codec.unboundedMap(Item.CODEC, ExtraCodecs.POSITIVE_FLOAT).fieldOf("loot_multipliers_for_hoes").forGetter(CropRightClickConfig::modifiers),
                    Codec.BOOL.fieldOf("enable_remove_list").forGetter(CropRightClickConfig::removeListEnabled),
                    Ingredient.CODEC.listOf().fieldOf("items_removed_from_loot").forGetter(CropRightClickConfig::removeList)
            ).apply(instance, CropRightClickConfig::new)
    );

    private static Map<Holder<Item>, Float> defaultModifier() {
        Map<Holder<Item>, Float> result = new HashMap<>();
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.WOODEN_HOE), 1.0F);
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.STONE_HOE), 1.0F);
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.GOLDEN_HOE), 1.5F);
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_HOE), 1.5F);
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND_HOE), 2.0F);
        result.put(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHERITE_HOE), 3.0F);
        return result;
    }

}
