package com.tristankechlo.improvedvanilla.config.util;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ImprovedVanillaConfig;
import com.tristankechlo.improvedvanilla.platform.IPlatformHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public final class ConfigManager {

    private static final File CONFIG_DIR = IPlatformHelper.INSTANCE.getConfigDirectory().toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    public static final String FILE_NAME = ImprovedVanilla.MOD_ID + ".json";
    private static final File CONFIG_FILE = new File(CONFIG_DIR, FILE_NAME);

    public static void loadAndVerifyConfig() {
        ConfigManager.createConfigFolder();

        if (!CONFIG_FILE.exists()) {
            ImprovedVanillaConfig.setToDefault();
            ConfigManager.writeConfigToFile();
            ImprovedVanilla.LOGGER.warn("No config '{}' was found, created a new one.", FILE_NAME);
            return;
        }

        try {
            ConfigManager.loadConfigFromFile();
            ImprovedVanilla.LOGGER.info("Config '{}' was successfully loaded.", FILE_NAME);
        } catch (Exception e) {
            ImprovedVanilla.LOGGER.error(e.getMessage());
            ImprovedVanilla.LOGGER.error("Error loading config '{}', config hasn't been loaded. Using default config.", FILE_NAME);
            ImprovedVanillaConfig.setToDefault();
        }
    }

    private static void loadConfigFromFile() throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(CONFIG_FILE));
        JsonObject json = jsonElement.getAsJsonObject();
        ImprovedVanillaConfig.deserialize(json);
    }

    private static void writeConfigToFile() {
        try {
            JsonElement jsonObject = ImprovedVanillaConfig.serialize(new JsonObject());
            JsonWriter writer = new JsonWriter(new FileWriter(CONFIG_FILE));
            writer.setIndent("\t");
            GSON.toJson(jsonObject, writer);
            writer.close();
        } catch (Exception e) {
            ImprovedVanilla.LOGGER.error("There was an error writing the config to file: '{}'", FILE_NAME);
            ImprovedVanilla.LOGGER.error(e.getMessage());
        }
    }

    public static void resetConfig() {
        ImprovedVanillaConfig.setToDefault();
        ConfigManager.writeConfigToFile();
        ImprovedVanilla.LOGGER.info("Config '{}' was set to default.", FILE_NAME);
    }

    public static void reloadConfig() {
        if (CONFIG_FILE.exists()) {
            try {
                ConfigManager.loadConfigFromFile();
                ImprovedVanilla.LOGGER.info("The config '{}' was successfully loaded.", FILE_NAME);
            } catch (Exception e) {
                ImprovedVanilla.LOGGER.error(e.getMessage());
                ImprovedVanilla.LOGGER.error("Error loading config '{}', config hasn't been loaded. Using the default config.", FILE_NAME);
                ImprovedVanillaConfig.setToDefault();
            }
        } else {
            ImprovedVanillaConfig.setToDefault();
            ConfigManager.writeConfigToFile();
            ImprovedVanilla.LOGGER.warn("No config '{}' found, created a new one.", FILE_NAME);
        }
    }

    public static String getConfigPath() {
        return CONFIG_FILE.getAbsolutePath();
    }

    private static void createConfigFolder() {
        if (!CONFIG_DIR.exists()) {
            if (!CONFIG_DIR.mkdirs()) {
                throw new RuntimeException("Could not create config folder: " + CONFIG_DIR.getAbsolutePath());
            }
        }
    }
}
