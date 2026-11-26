package dev.t1deedyn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ModConfig {
    private static final Path FILE = Path.of("config", "t1deedyn.properties");
    public static boolean particles = true;
    public static boolean cleanHud = false;
    public static boolean showCoordinates = true;
    public static boolean lowFire = true;
    public static boolean smoothCamera = false;
    public static boolean menuAnimations = true;
    public static double uiScale = 1.0;
    public static double menuOpacity = 0.94;
    public static double backgroundDim = 0.48;
    public static int accentTheme = 0;
    public static boolean skyVisual = true;
    public static boolean rgbLighting = true;
    public static boolean cursorTrail = true;

    private ModConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            particles = Boolean.parseBoolean(p.getProperty("particles", "true"));
            cleanHud = Boolean.parseBoolean(p.getProperty("cleanHud", "false"));
            showCoordinates = Boolean.parseBoolean(p.getProperty("showCoordinates", "true"));
            lowFire = Boolean.parseBoolean(p.getProperty("lowFire", "true"));
            smoothCamera = Boolean.parseBoolean(p.getProperty("smoothCamera", "false"));
            menuAnimations = Boolean.parseBoolean(p.getProperty("menuAnimations", "true"));
            uiScale = clamp(parse(p, "uiScale", 1.0), 0.80, 1.15);
            menuOpacity = clamp(parse(p, "menuOpacity", 0.94), 0.72, 1.0);
            backgroundDim = clamp(parse(p, "backgroundDim", 0.48), 0.0, 0.75);
            accentTheme = Math.max(0, Math.min(2, Integer.parseInt(p.getProperty("accentTheme", "0"))));
            skyVisual = Boolean.parseBoolean(p.getProperty("skyVisual", "true"));
            rgbLighting = Boolean.parseBoolean(p.getProperty("rgbLighting", "true"));
            cursorTrail = Boolean.parseBoolean(p.getProperty("cursorTrail", "true"));
        } catch (IOException | NumberFormatException ignored) { }
    }

    private static double parse(Properties p, String key, double fallback) {
        return Double.parseDouble(p.getProperty(key, Double.toString(fallback)));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("particles", Boolean.toString(particles));
        p.setProperty("cleanHud", Boolean.toString(cleanHud));
        p.setProperty("showCoordinates", Boolean.toString(showCoordinates));
        p.setProperty("lowFire", Boolean.toString(lowFire));
        p.setProperty("smoothCamera", Boolean.toString(smoothCamera));
        p.setProperty("menuAnimations", Boolean.toString(menuAnimations));
        p.setProperty("uiScale", Double.toString(uiScale));
        p.setProperty("menuOpacity", Double.toString(menuOpacity));
        p.setProperty("backgroundDim", Double.toString(backgroundDim));
        p.setProperty("accentTheme", Integer.toString(accentTheme));
        p.setProperty("skyVisual", Boolean.toString(skyVisual));
        p.setProperty("rgbLighting", Boolean.toString(rgbLighting));
        p.setProperty("cursorTrail", Boolean.toString(cursorTrail));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "T1deedyn settings"); }
        } catch (IOException ignored) { }
    }
}
