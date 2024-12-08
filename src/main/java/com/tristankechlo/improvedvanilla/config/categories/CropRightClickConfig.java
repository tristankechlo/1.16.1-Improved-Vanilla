package com.tristankechlo.improvedvanilla.config.categories;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.values.BooleanValue;
import com.tristankechlo.improvedvanilla.config.values.SetValue;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public final class CropRightClickConfig {

    private static final String IDENTIFIER = "right_click_to_harvest";
    private static final Function<Item, String> ITEM_TO_STRING = (item) -> ForgeRegistries.ITEMS.getKey(item).toString();
    private static final Function<String, Item> STRING_TO_ITEM = (string) -> {
        ResourceLocation resourceLocation = new ResourceLocation(string);
        return ForgeRegistries.ITEMS.getValue(resourceLocation);
    };

    public final BooleanValue activated = new BooleanValue("activated", true);
    public final BooleanValue allowHoeUsageAsLootModifier = new BooleanValue("allow_hoe_usage_as_loot_modifier", true);
    public final BooleanValue blacklistEnabled = new BooleanValue("enable_blacklist", false);
    public final SetValue<Item> blacklistedDrops = new SetValue<>("blacklisted_drops", ImmutableSet.of(Items.WHEAT_SEEDS), ITEM_TO_STRING, STRING_TO_ITEM);

    public void setToDefault() {
        activated.setToDefault();
        allowHoeUsageAsLootModifier.setToDefault();
        blacklistEnabled.setToDefault();
        blacklistedDrops.setToDefault();
    }

    public void serialize(JsonObject json) {
        JsonObject object = new JsonObject();

        activated.serialize(object);
        allowHoeUsageAsLootModifier.serialize(object);
        blacklistEnabled.serialize(object);
        blacklistedDrops.serialize(object);

        json.add(IDENTIFIER, object);
    }

    public void deserialize(JsonObject json) {
        if (json.has(IDENTIFIER)) {
            JsonObject object = json.getAsJsonObject(IDENTIFIER);

            activated.deserialize(object);
            allowHoeUsageAsLootModifier.deserialize(object);
            blacklistEnabled.deserialize(object);
            blacklistedDrops.deserialize(object);
        }
    }

}
