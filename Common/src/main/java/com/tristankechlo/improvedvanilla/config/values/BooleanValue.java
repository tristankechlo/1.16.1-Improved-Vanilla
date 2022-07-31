package com.tristankechlo.improvedvanilla.config.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.Constants;

public final class BooleanValue implements IConfigValue<Boolean> {

    private final String identifier;
    private final Boolean defaultValue;
    private Boolean value;

    public BooleanValue(String identifier, Boolean defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
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
    public Boolean get() {
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
                value = element.getAsBoolean();
                return;
            }
        } catch (Exception e) {
            Constants.LOGGER.warn("Error while loading the config value " + getIdentifier() + ", using defaultvalue instead");
        }
        this.setToDefault();
    }

}
