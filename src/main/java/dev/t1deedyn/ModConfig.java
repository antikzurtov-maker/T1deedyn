package dev.t1deedyn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
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

    private ModConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
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
            accentTheme = Math.max(0, Math.min(2, integer(j, "accentTheme", 0)));
            skyVisual = bool(j, "skyVisual", skyVisual);
            rgbLighting = bool(j, "rgbLighting", rgbLighting);
        } catch (Exception ignored) {
            // Invalid config is ignored; defaults remain active.
        }
    }

    public static void save() {
        JsonObject j = new JsonObject();
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
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(j));
        } catch (IOException ignored) {
            // Config write failures must not interrupt rendering.
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

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
