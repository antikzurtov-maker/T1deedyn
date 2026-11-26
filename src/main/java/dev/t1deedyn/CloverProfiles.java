package dev.t1deedyn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JSON snapshots of the public ModConfig settings. */
public final class CloverProfiles {
    private static final Path DIRECTORY = Path.of("config", "black-clover-profiles");

    private CloverProfiles() {}

    public static List<String> list() {
        try {
            if (!Files.isDirectory(DIRECTORY)) return List.of();
            try (var files = Files.list(DIRECTORY)) {
                return files.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                        .sorted(String.CASE_INSENSITIVE_ORDER).toList();
            }
        } catch (Exception exception) {
            T1deedynClient.LOGGER.warn("Could not list Black Clover profiles", exception);
            return List.of();
        }
    }

    public static boolean save(String name) {
        Path file = profilePath(name);
        if (file == null) return false;
        try {
            Files.createDirectories(DIRECTORY);
            JsonObject json = new JsonObject();
            for (Field field : ModConfig.class.getFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;
                Class<?> type = field.getType();
                if (type != boolean.class && type != int.class && type != double.class && type != String.class) continue;
                Object value = field.get(null);
                if (value instanceof Boolean v) json.addProperty(field.getName(), v);
                else if (value instanceof Integer v) json.addProperty(field.getName(), v);
                else if (value instanceof Double v) json.addProperty(field.getName(), v);
                else if (value instanceof String v) json.addProperty(field.getName(), v);
            }
            Files.writeString(file, json.toString());
            return true;
        } catch (Exception exception) {
            T1deedynClient.LOGGER.warn("Could not save Black Clover profile {}", name, exception);
            return false;
        }
    }

    public static boolean load(String name) {
        Path file = profilePath(name);
        if (file == null || !Files.isRegularFile(file)) return false;
        try {
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            for (Field field : ModConfig.class.getFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || !json.has(field.getName())) continue;
                Class<?> type = field.getType();
                if (type == boolean.class) field.setBoolean(null, json.get(field.getName()).getAsBoolean());
                else if (type == int.class) field.setInt(null, json.get(field.getName()).getAsInt());
                else if (type == double.class) field.setDouble(null, json.get(field.getName()).getAsDouble());
                else if (type == String.class) field.set(null, json.get(field.getName()).getAsString());
            }
            ModConfig.save();
            Theme.select(Theme.Preset.values()[Math.max(0, Math.min(Theme.Preset.values().length - 1, ModConfig.accentTheme))]);
            return true;
        } catch (Exception exception) {
            T1deedynClient.LOGGER.warn("Could not load Black Clover profile {}", name, exception);
            return false;
        }
    }

    public static boolean rename(String oldName, String newName) {
        Path source = profilePath(oldName);
        Path target = profilePath(newName);
        if (source == null || target == null || !Files.isRegularFile(source) || Files.exists(target)) return false;
        try {
            Files.move(source, target);
            return true;
        } catch (Exception exception) {
            T1deedynClient.LOGGER.warn("Could not rename Black Clover profile {} to {}", oldName, newName, exception);
            return false;
        }
    }

    public static boolean delete(String name) {
        Path file = profilePath(name);
        if (file == null) return false;
        try { return Files.deleteIfExists(file); }
        catch (Exception exception) {
            T1deedynClient.LOGGER.warn("Could not delete Black Clover profile {}", name, exception);
            return false;
        }
    }

    private static Path profilePath(String name) {
        if (name == null) return null;
        String safe = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        if (safe.isBlank() || safe.length() > 32) return null;
        return DIRECTORY.resolve(safe + ".json");
    }
}
