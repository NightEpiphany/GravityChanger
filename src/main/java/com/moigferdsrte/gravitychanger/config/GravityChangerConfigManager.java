package com.moigferdsrte.gravitychanger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moigferdsrte.gravitychanger.GravityChanger;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.loader.api.FabricLoader;

public final class GravityChangerConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("gravity_changer.json");
    private static final AtomicReference<GravityChangerConfig> CURRENT = new AtomicReference<>(GravityChangerConfig.defaults());

    private GravityChangerConfigManager() {
    }

    public static GravityChangerConfig get() {
        return CURRENT.get();
    }

    public static synchronized void initialize() {
        if (!Files.exists(CONFIG_PATH)) {
            save(GravityChangerConfig.defaults());
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            GravityChangerConfig loaded = new GravityChangerConfig(
                getBoolean(root, "directionalFallLimitEnabled", GravityChangerConfig.DEFAULT_DIRECTIONAL_FALL_LIMIT_ENABLED),
                getInt(root, "directionalFallLimitSeconds", GravityChangerConfig.DEFAULT_DIRECTIONAL_FALL_LIMIT_SECONDS)
            );
            CURRENT.set(loaded);
        } catch (Exception exception) {
            GravityChanger.LOGGER.error("Failed to load {}, using defaults", CONFIG_PATH, exception);
            save(GravityChangerConfig.defaults());
        }
    }

    public static synchronized void save(final GravityChangerConfig config) {
        GravityChangerConfig normalized = new GravityChangerConfig(
            config.directionalFallLimitEnabled(),
            config.directionalFallLimitSeconds()
        );
        CURRENT.set(normalized);

        Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
                GSON.toJson(toJson(normalized), writer);
            }

            try {
                Files.move(
                    temporaryPath,
                    CONFIG_PATH,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            GravityChanger.LOGGER.error("Failed to save {}", CONFIG_PATH, exception);
        } finally {
            try {
                Files.deleteIfExists(temporaryPath);
            } catch (IOException exception) {
                GravityChanger.LOGGER.warn("Failed to remove temporary config file {}", temporaryPath, exception);
            }
        }
    }

    private static JsonObject toJson(final GravityChangerConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("directionalFallLimitEnabled", config.directionalFallLimitEnabled());
        root.addProperty("directionalFallLimitSeconds", config.directionalFallLimitSeconds());
        return root;
    }

    private static boolean getBoolean(final JsonObject root, final String key, final boolean defaultValue) {
        return root.has(key) ? root.get(key).getAsBoolean() : defaultValue;
    }

    private static int getInt(final JsonObject root, final String key, final int defaultValue) {
        return root.has(key) ? root.get(key).getAsInt() : defaultValue;
    }
}
