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
    public static double uiScale = 1.0;

    private ModConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            particles = Boolean.parseBoolean(p.getProperty("particles", "true"));
            cleanHud = Boolean.parseBoolean(p.getProperty("cleanHud", "false"));
            uiScale = Math.max(0.75, Math.min(1.25, Double.parseDouble(p.getProperty("uiScale", "1.0"))));
        } catch (IOException | NumberFormatException ignored) { }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("particles", Boolean.toString(particles));
        p.setProperty("cleanHud", Boolean.toString(cleanHud));
        p.setProperty("uiScale", Double.toString(uiScale));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "T1deedyn settings"); }
        } catch (IOException ignored) { }
    }
}
