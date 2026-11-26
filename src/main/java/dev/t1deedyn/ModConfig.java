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
            uiScale = Math.max(0.75, Math.min(1.25, Double.parseDouble(p.getProperty("uiScale", "1.0"))));
        } catch (IOException | NumberFormatException ignored) { }
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
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "T1deedyn settings"); }
        } catch (IOException ignored) { }
    }
}
