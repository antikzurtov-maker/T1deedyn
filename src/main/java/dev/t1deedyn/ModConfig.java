package dev.t1deedyn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final int CONFIG_VERSION = 1;
    private static final Path FILE = Path.of("config", "t1deedyn.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean particles = true;
    public static boolean cleanHud = false;
    public static boolean showCoordinates = true;
    public static boolean lowFire = true;
    public static boolean smoothCamera = false;
    public static boolean menuAnimations = true;
    public static final int uiScale = 1;
    public static double menuOpacity = 0.94;
    public static double backgroundDim = 0.35;
    public static int accentTheme = 0;
    public static boolean skyVisual = true;
    public static boolean rgbLighting = true;
    public static boolean menuBlur = false;
    public static boolean showFps = true;
    public static boolean showPing = true;
    public static boolean showHudCoordinates = true;
    public static boolean autoPerformance = true;
    public static int effectFps = 30;
    public static int particleLimit = 24;

    private ModConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            JsonObject j = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            particles = bool(j, "particles", particles);
            cleanHud = bool(j, "cleanHud", cleanHud);
            showCoordinates = bool(j, "showCoordinates", showCoordinates);
            lowFire = bool(j, "lowFire", lowFire);
            smoothCamera = bool(j, "smoothCamera", smoothCamera);
            menuAnimations = bool(j, "menuAnimations", menuAnimations);
            menuOpacity = clamp(number(j, "menuOpacity", menuOpacity), 0.72, 1.0);
            backgroundDim = clamp(number(j, "backgroundDim", backgroundDim), 0.0, 0.75);
            accentTheme = clampInt(integer(j, "accentTheme", 0), 0, 2);
            skyVisual = bool(j, "skyVisual", skyVisual);
            rgbLighting = bool(j, "rgbLighting", rgbLighting);
            menuBlur = bool(j, "menuBlur", false);
            showFps = bool(j, "showFps", true);
            showPing = bool(j, "showPing", true);
            showHudCoordinates = bool(j, "showHudCoordinates", true);
            autoPerformance = bool(j, "autoPerformance", true);
            effectFps = clampInt(integer(j, "effectFps", 30), 5, 60);
            particleLimit = clampInt(integer(j, "particleLimit", 24), 0, 128);
            int loadedVersion = integer(j, "configVersion", 0);
            if (loadedVersion != CONFIG_VERSION) save();
        } catch (Exception exception) {
            save();
        }
    }

    public static void save() {
        JsonObject j = new JsonObject();
        j.addProperty("configVersion", CONFIG_VERSION);
        j.addProperty("particles", particles);
        j.addProperty("cleanHud", cleanHud);
        j.addProperty("showCoordinates", showCoordinates);
        j.addProperty("lowFire", lowFire);
        j.addProperty("smoothCamera", smoothCamera);
        j.addProperty("menuAnimations", menuAnimations);
        j.addProperty("uiScale", uiScale);
        j.addProperty("menuOpacity", menuOpacity);
        j.addProperty("backgroundDim", backgroundDim);
        j.addProperty("accentTheme", accentTheme);
        j.addProperty("skyVisual", skyVisual);
        j.addProperty("rgbLighting", rgbLighting);
        j.addProperty("menuBlur", menuBlur);
        j.addProperty("showFps", showFps);
        j.addProperty("showPing", showPing);
        j.addProperty("showHudCoordinates", showHudCoordinates);
        j.addProperty("autoPerformance", autoPerformance);
        j.addProperty("effectFps", effectFps);
        j.addProperty("particleLimit", particleLimit);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(j));
        } catch (IOException exception) {
            T1deedynClient.LOGGER.warn("Could not save T1deedyn config: {}", exception.getMessage());
        }
    }

    private static boolean bool(JsonObject j, String key, boolean fallback) {
        return j.has(key) && !j.get(key).isJsonNull() ? j.get(key).getAsBoolean() : fallback;
    }

    private static double number(JsonObject j, String key, double fallback) {
        return j.has(key) && !j.get(key).isJsonNull() ? j.get(key).getAsDouble() : fallback;
    }

    private static int integer(JsonObject j, String key, int fallback) {
        return j.has(key) && !j.get(key).isJsonNull() ? j.get(key).getAsInt() : fallback;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
