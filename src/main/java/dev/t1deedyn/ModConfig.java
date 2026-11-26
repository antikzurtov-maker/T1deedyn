package dev.t1deedyn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    public static final String BUILD_VERSION = "0.3.0";
    private static final int CONFIG_VERSION = 6;
    private static final Path FILE = Path.of("config", "t1deedyn.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean particles = true;
    public static boolean cleanHud = false;
    public static boolean showCoordinates = true;
    public static boolean lowFire = true;
    public static boolean smoothCamera = false;
    public static boolean menuAnimations = true;
    public static int uiScale = 0; // 0 = Auto, 1..4 = fixed menu GUI scale
    public static boolean showBounds = false;
    public static double menuOpacity = 0.94;
    public static double backgroundDim = 0.25;
    public static int accentTheme = 0;
    public static String accentHex = "#2BE36B";
    public static double accentBrightness = 1.0;
    public static boolean skyVisual = true;
    public static boolean rgbLighting = true;
    public static boolean showFps = true;
    public static boolean showPing = true;
    public static boolean showHudCoordinates = true;
    public static boolean autoPerformance = true;
    public static int effectFps = 30;
    public static int particleLimit = 24;

    public static boolean autoBerryEnabled = false;
    public static int autoBerryToggleKey = -1;
    public static boolean autoBerryAccordion = false;
    public static double autoBerryRadius = 3.0;
    public static boolean autoBerryLimitReach = true;
    public static boolean autoBerrySweet = true;
    public static boolean autoBerryGlow = true;
    public static boolean autoBerryStageTwo = false;
    public static int autoBerryClickDelay = 3;
    public static int autoBerryRandomDelay = 1;
    public static int autoBerryMaxCps = 5;
    public static int autoBerryScanTicks = 5;
    public static boolean autoBerryOnlyStationary = false;
    public static boolean autoBerryPauseWithScreen = true;
    public static boolean autoBerryPauseUsingItem = true;
    public static boolean autoBerrySafeHand = true;
    public static boolean autoBerrySwing = true;
    public static boolean autoBerrySound = true;
    public static boolean autoBerryParticles = false;
    public static boolean autoBerryCountHud = true;
    public static boolean autoBerryHighlight = false;
    public static boolean autoBerryVisibleOnly = false;
    public static int berryHighlightRadius = 8;
    public static int berryHighlightScanTicks = 5;
    public static int berryHighlightColor = 0xFF55E58A;
    public static boolean berryHighlightDebug = false;

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
            accentTheme = clampInt(integer(j, "accentTheme", 0), 0, 4);
            accentHex = validHex(string(j, "accentHex", accentHex)) ? string(j, "accentHex", accentHex).toUpperCase(java.util.Locale.ROOT) : "#2BE36B";
            uiScale = clampInt(integer(j, "uiScale", 0), 0, 4);
            showBounds = bool(j, "showBounds", false);
            accentBrightness = clamp(number(j, "accentBrightness", 1.0), 0.1, 1.0);
            skyVisual = bool(j, "skyVisual", skyVisual);
            rgbLighting = bool(j, "rgbLighting", rgbLighting);
            showFps = bool(j, "showFps", true);
            showPing = bool(j, "showPing", true);
            showHudCoordinates = bool(j, "showHudCoordinates", true);
            autoPerformance = bool(j, "autoPerformance", true);
            effectFps = clampInt(integer(j, "effectFps", 30), 5, 60);
            particleLimit = clampInt(integer(j, "particleLimit", 24), 0, 128);
            int loadedVersion = integer(j, "configVersion", 0);
            autoBerryEnabled = bool(j, "autoBerryEnabled", false);
            autoBerryToggleKey = clampInt(integer(j, "autoBerryToggleKey", -1), -1, 348);
            autoBerryAccordion = bool(j, "autoBerryAccordion", false);
            autoBerryRadius = clamp(number(j, "autoBerryRadius", 3.0), 1.0, 5.0);
            autoBerryLimitReach = bool(j, "autoBerryLimitReach", true);
            autoBerrySweet = bool(j, "autoBerrySweet", true);
            autoBerryGlow = bool(j, "autoBerryGlow", true);
            autoBerryStageTwo = bool(j, "autoBerryStageTwo", false);
            autoBerryClickDelay = clampInt(integer(j, "autoBerryClickDelay", 3), 1, 20);
            autoBerryRandomDelay = clampInt(integer(j, "autoBerryRandomDelay", 1), 0, 5);
            autoBerryMaxCps = clampInt(integer(j, "autoBerryMaxCps", 5), 1, 10);
            autoBerryScanTicks = clampInt(integer(j, "autoBerryScanTicks", 5), 1, 20);
            autoBerryOnlyStationary = bool(j, "autoBerryOnlyStationary", false);
            autoBerryPauseWithScreen = bool(j, "autoBerryPauseWithScreen", true);
            autoBerryPauseUsingItem = bool(j, "autoBerryPauseUsingItem", true);
            autoBerrySafeHand = bool(j, "autoBerrySafeHand", true);
            autoBerrySwing = bool(j, "autoBerrySwing", true);
            autoBerrySound = bool(j, "autoBerrySound", true);
            autoBerryParticles = bool(j, "autoBerryParticles", false);
            autoBerryCountHud = bool(j, "autoBerryCountHud", true);
            autoBerryHighlight = bool(j, "autoBerryHighlight", false);
            autoBerryVisibleOnly = bool(j, "autoBerryVisibleOnly", false);
            berryHighlightRadius = clampInt(integer(j, "berryHighlightRadius", 8), 1, 16);
            berryHighlightScanTicks = clampInt(integer(j, "berryHighlightScanTicks", 5), 1, 40);
            berryHighlightColor = integer(j, "berryHighlightColor", 0xFF55E58A) | 0xFF000000;
            berryHighlightDebug = bool(j, "berryHighlightDebug", false);
            // Older configs limited discovery to interaction reach, missing bushes ahead of a minecart.
            if (loadedVersion < 4) {
                autoBerryLimitReach = false;
                autoBerryRadius = Math.max(autoBerryRadius, 5.0);
            }
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
        j.addProperty("showBounds", showBounds);
        j.addProperty("menuOpacity", menuOpacity);
        j.addProperty("backgroundDim", backgroundDim);
        j.addProperty("accentTheme", accentTheme);
        j.addProperty("accentHex", accentHex);
        j.addProperty("accentBrightness", accentBrightness);
        j.addProperty("skyVisual", skyVisual);
        j.addProperty("rgbLighting", rgbLighting);
        j.addProperty("showFps", showFps);
        j.addProperty("showPing", showPing);
        j.addProperty("showHudCoordinates", showHudCoordinates);
        j.addProperty("autoPerformance", autoPerformance);
        j.addProperty("effectFps", effectFps);
        j.addProperty("particleLimit", particleLimit);
        j.addProperty("autoBerryEnabled", autoBerryEnabled);
        j.addProperty("autoBerryToggleKey", autoBerryToggleKey);
        j.addProperty("autoBerryAccordion", autoBerryAccordion);
        j.addProperty("autoBerryRadius", autoBerryRadius);
        j.addProperty("autoBerryLimitReach", autoBerryLimitReach);
        j.addProperty("autoBerrySweet", autoBerrySweet);
        j.addProperty("autoBerryGlow", autoBerryGlow);
        j.addProperty("autoBerryStageTwo", autoBerryStageTwo);
        j.addProperty("autoBerryClickDelay", autoBerryClickDelay);
        j.addProperty("autoBerryRandomDelay", autoBerryRandomDelay);
        j.addProperty("autoBerryMaxCps", autoBerryMaxCps);
        j.addProperty("autoBerryScanTicks", autoBerryScanTicks);
        j.addProperty("autoBerryOnlyStationary", autoBerryOnlyStationary);
        j.addProperty("autoBerryPauseWithScreen", autoBerryPauseWithScreen);
        j.addProperty("autoBerryPauseUsingItem", autoBerryPauseUsingItem);
        j.addProperty("autoBerrySafeHand", autoBerrySafeHand);
        j.addProperty("autoBerrySwing", autoBerrySwing);
        j.addProperty("autoBerrySound", autoBerrySound);
        j.addProperty("autoBerryParticles", autoBerryParticles);
        j.addProperty("autoBerryCountHud", autoBerryCountHud);
        j.addProperty("autoBerryHighlight", autoBerryHighlight);
        j.addProperty("autoBerryVisibleOnly", autoBerryVisibleOnly);
        j.addProperty("berryHighlightRadius", berryHighlightRadius);
        j.addProperty("berryHighlightScanTicks", berryHighlightScanTicks);
        j.addProperty("berryHighlightColor", berryHighlightColor);
        j.addProperty("berryHighlightDebug", berryHighlightDebug);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(j));
        } catch (IOException exception) {
            T1deedynClient.LOGGER.warn("Could not save T1deedyn config: {}", exception.getMessage());
        }
    }

    private static String string(JsonObject j, String key, String fallback) { return j.has(key) && !j.get(key).isJsonNull() ? j.get(key).getAsString() : fallback; }

    private static boolean validHex(String value) { return value != null && value.matches("#[0-9a-fA-F]{6}"); }

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
