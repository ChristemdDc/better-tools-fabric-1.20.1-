package com.bettertools.config;

import com.bettertools.BetterToolsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BetterToolsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bettertools.json");

    public static Map<String, ToolConfig> toolConfigs = new HashMap<>();

    public static final String[] TOOL_TYPES = {"pickaxe", "axe", "shovel", "sword", "hoe"};

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null && data.toolConfigs != null) {
                    toolConfigs = data.toolConfigs;
                }
            } catch (IOException e) {
                BetterToolsMod.LOGGER.error("Failed to load Better Tools config", e);
            }
        }
        for (String toolType : TOOL_TYPES) {
            toolConfigs.putIfAbsent(toolType, new ToolConfig());
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            ConfigData data = new ConfigData();
            data.toolConfigs = toolConfigs;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            BetterToolsMod.LOGGER.error("Failed to save Better Tools config", e);
        }
    }

    public static ToolConfig getConfig(String toolType) {
        return toolConfigs.computeIfAbsent(toolType, k -> new ToolConfig());
    }

    private static class ConfigData {
        Map<String, ToolConfig> toolConfigs = new HashMap<>();
    }
}
