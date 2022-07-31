package com.tristankechlo.improvedvanilla.config.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.Constants;

public final class IntegerValue implements IConfigValue<Integer> {

    private final String identifier;
    private final Integer defaultValue;
    private Integer value;
    private Integer minValue;
    private Integer maxValue;

    public IntegerValue(String identifier, Integer defaultValue, Integer minValue, Integer maxValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setToDefault() {
        this.value = defaultValue;
    }

    @Override
    public Integer get() {
        return value;
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(getIdentifier(), get());
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        try {
            JsonElement element = jsonObject.get(getIdentifier());
            if (element != null) {
                value = element.getAsInt();
                return;
            }
        } catch (Exception e) {
            Constants.LOGGER.warn("Error while loading the config value " + getIdentifier() + ", using defaultvalue instead");
        }
        this.setToDefault();
    }

}
