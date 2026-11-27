package dev.t1deedyn;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Allocation-conscious CustomSky palette engine for Minecraft 1.21.0.
 * It keeps vanilla geometry and render state intact for mobile renderer compatibility.
 */
public final class CustomSkyRenderer {
    private static final float[] SUNSET = {0.92f, 0.31f, 0.28f};
    private static final float[] SUNRISE = {0.98f, 0.56f, 0.36f};
    private static final float[] NIGHT = {0.025f, 0.035f, 0.12f};
    private static final float[] STARRY = {0.035f, 0.045f, 0.14f};
    private static final float[] GALAXY = {0.10f, 0.055f, 0.22f};
    private static final float[] PURPLE = {0.25f, 0.10f, 0.48f};
    private static final float[] BLOOD_MOON = {0.34f, 0.035f, 0.055f};
    private static final float[] AURORA = {0.035f, 0.20f, 0.22f};
    private static final float[] FANTASY = {0.28f, 0.09f, 0.39f};
    private static final float[] DAY = {0.36f, 0.62f, 0.94f};
    private static final float[] DEEP_NIGHT = {0.045f, 0.055f, 0.16f};
    private static final float[] DYNAMIC = new float[3];
    private static final float[] CUSTOM = new float[3];
    private static final float[] ADJUSTED = new float[3];
    private static final String[] MODE_NAMES = {
            "Vanilla", "Sunset", "Sunrise", "Night", "Starry",
            "Galaxy", "Purple", "BloodMoon", "Aurora", "Fantasy", "Custom"
    };

    private CustomSkyRenderer() {}

    public static String modeName() {
        return MODE_NAMES[clamp(ModConfig.skyMode, 0, MODE_NAMES.length - 1)];
    }

    public static Vec3d skyColor(ClientWorld world, float tickDelta, Vec3d vanilla) {
        if (!active(world)) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 10);
        if (mode == 0) return vanilla;

        float day = dayPhase(world);
        float[] rgb = adjustColor(palette(mode, day), (float) ModConfig.skyBrightness, (float) ModConfig.skySaturation);
        float intensity = (float) clamp(ModConfig.skyIntensity, 0.0, 1.0) * qualityFactor();
        float rain = world.getRainGradient(tickDelta);
        float thunder = world.getThunderGradient(tickDelta);
        float weatherDim = 1.0f - rain * 0.14f - thunder * 0.20f;

        float r = mix((float) vanilla.x, rgb[0], intensity) * weatherDim;
        float g = mix((float) vanilla.y, rgb[1], intensity) * weatherDim;
        float b = mix((float) vanilla.z, rgb[2], intensity) * weatherDim;
        return new Vec3d(clamp(r, 0.0f, 1.0f), clamp(g, 0.0f, 1.0f), clamp(b, 0.0f, 1.0f));
    }

    public static Vec3d cloudColor(ClientWorld world, float tickDelta, Vec3d vanilla) {
        if (!active(world) || ModConfig.skyMode == 0) return vanilla;
        float[] rgb = adjustColor(palette(clamp(ModConfig.skyMode, 0, 10), dayPhase(world)), (float) ModConfig.skyBrightness, (float) ModConfig.skySaturation);
        float rain = world.getRainGradient(tickDelta);
        float amount = (float) (clamp(ModConfig.skyIntensity, 0.0, 1.0) * 0.24 * qualityFactor());
        float weatherDim = 1.0f - rain * 0.12f;
        return new Vec3d(
                clamp(mix((float) vanilla.x, rgb[0], amount) * weatherDim, 0.0f, 1.0f),
                clamp(mix((float) vanilla.y, rgb[1], amount) * weatherDim, 0.0f, 1.0f),
                clamp(mix((float) vanilla.z, rgb[2], amount) * weatherDim, 0.0f, 1.0f));
    }

    public static float starBrightness(ClientWorld world, float vanilla) {
        if (!active(world) || ModConfig.skyMode == 0) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 10);
        float multiplier = switch (mode) {
            case 3 -> 1.08f;
            case 4, 5, 6, 7, 8, 9, 10 -> 1.24f;
            default -> 1.0f;
        };
        float amount = (float) clamp(ModConfig.skyIntensity, 0.0, 1.0) * qualityFactor();
        return clamp(vanilla * mix(1.0f, multiplier, amount), 0.0f, 1.0f);
    }

    private static boolean active(ClientWorld world) {
        return ModConfig.customSkyEnabled
                && world != null
                && World.OVERWORLD.equals(world.getRegistryKey());
    }

    private static float dayPhase(ClientWorld world) {
        return (float) ((world.getTimeOfDay() % 24000L + 24000L) % 24000L) / 24000.0f;
    }

    private static float[] palette(int mode, float day) {
        float[] base = switch (mode) {
            case 1 -> SUNSET;
            case 2 -> SUNRISE;
            case 3 -> NIGHT;
            case 4 -> STARRY;
            case 5 -> GALAXY;
            case 6 -> PURPLE;
            case 7 -> BLOOD_MOON;
            case 8 -> AURORA;
            case 9 -> FANTASY;
            case 10 -> customPalette();
            default -> DAY;
        };

        if (mode == 3 || mode == 4 || mode == 5 || mode == 7) return base;

        float daylight = (float) Math.sin(day * Math.PI);
        float nightBlend = (1.0f - daylight) * 0.12f;
        DYNAMIC[0] = mix(base[0], DEEP_NIGHT[0], nightBlend);
        DYNAMIC[1] = mix(base[1], DEEP_NIGHT[1], nightBlend);
        DYNAMIC[2] = mix(base[2], DEEP_NIGHT[2], nightBlend);
        return DYNAMIC;
    }

    private static float[] customPalette() {
        int color = ModConfig.skyCustomColor;
        CUSTOM[0] = ((color >>> 16) & 255) / 255.0f;
        CUSTOM[1] = ((color >>> 8) & 255) / 255.0f;
        CUSTOM[2] = (color & 255) / 255.0f;
        return CUSTOM;
    }

    private static float[] adjustColor(float[] rgb, float brightness, float saturation) {
        float luminance = rgb[0] * 0.2126f + rgb[1] * 0.7152f + rgb[2] * 0.0722f;
        ADJUSTED[0] = clamp((luminance + (rgb[0] - luminance) * saturation) * brightness, 0.0f, 1.0f);
        ADJUSTED[1] = clamp((luminance + (rgb[1] - luminance) * saturation) * brightness, 0.0f, 1.0f);
        ADJUSTED[2] = clamp((luminance + (rgb[2] - luminance) * saturation) * brightness, 0.0f, 1.0f);
        return ADJUSTED;
    }

    private static float qualityFactor() {
        return switch (clamp(ModConfig.skyQuality, 0, 2)) {
            case 0 -> 0.55f;
            case 2 -> 1.0f;
            default -> 0.8f;
        };
    }

    private static float mix(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
