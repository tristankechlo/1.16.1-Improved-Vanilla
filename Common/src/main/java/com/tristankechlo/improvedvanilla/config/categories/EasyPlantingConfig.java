package com.tristankechlo.improvedvanilla.config.categories;

import com.google.gson.JsonObject;
import com.tristankechlo.improvedvanilla.config.values.BooleanValue;
import com.tristankechlo.improvedvanilla.config.values.IntegerValue;

public class EasyPlantingConfig {

    private static final String IDENTIFIER = "easy_planting";

    public final BooleanValue activated = new BooleanValue("activated", true);
    public final IntegerValue radius = new IntegerValue("radius", 3, 1, 10);
    public final BooleanValue makeCircle = new BooleanValue("make_circle", true);

    public void setToDefault() {
        activated.setToDefault();
        radius.setToDefault();
        makeCircle.setToDefault();
    }

    public void serialize(JsonObject json) {
        JsonObject object = new JsonObject();

        activated.serialize(object);
        radius.serialize(object);
        makeCircle.serialize(object);

        json.add(IDENTIFIER, object);
    }

    public void deserialize(JsonObject json) {
        if (json.has(IDENTIFIER)) {
            JsonObject object = json.getAsJsonObject(IDENTIFIER);

            activated.deserialize(object);
            radius.deserialize(object);
            makeCircle.deserialize(object);
        }
    }

}
