package com.tristankechlo.improvedvanilla.config.values;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import net.minecraft.util.GsonHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class SetValue<T> implements IConfigValue<Set<T>> {

    private final String identifier;
    private final ImmutableSet<T> defaultValue;
    private final Set<T> value = new HashSet<>();
    private final Function<T, String> serialize;
    private final Function<String, T> deserialize;

    public SetValue(String identifier, ImmutableSet<T> defaultValue, Function<T, String> serialize, Function<String, T> deserialize) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
        this.value.addAll(defaultValue);
        this.serialize = serialize;
        this.deserialize = deserialize;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setToDefault() {
        this.value.clear();
        this.value.addAll(this.defaultValue);
    }

    @Override
    public Set<T> get() {
        return this.value;
    }

    @Override
    public void serialize(JsonObject json) {
        JsonArray jsonArray = new JsonArray();
        for (T t : this.value) {
            jsonArray.add(serialize.apply(t));
        }
        json.add(this.identifier, jsonArray);
    }

    @Override
    public void deserialize(JsonObject json) {
        try {
            if (GsonHelper.isArrayNode(json, getIdentifier())) {
                JsonArray jsonArray = json.getAsJsonArray(getIdentifier());
                this.value.clear();
                for (int i = 0; i < jsonArray.size(); i++) {
                    String stringElement = jsonArray.get(i).getAsString();
                    T element = deserialize.apply(stringElement);
                    this.value.add(element);
                }
                return;
            } else {
                ImprovedVanilla.LOGGER.warn("Config value '{}' was not found or is not an array, using default value instead", getIdentifier());
            }
        } catch (Exception e) {
            ImprovedVanilla.LOGGER.warn("Error while loading the config value '{}', using default value instead", getIdentifier());
        }
        this.setToDefault();
    }

}
