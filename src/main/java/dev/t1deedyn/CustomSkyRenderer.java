package dev.t1deedyn;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Lightweight palette controller. It changes the existing vanilla sky/cloud colours
 * and star brightness; it does not replace WorldRenderer or allocate geometry.
 */
public final class CustomSkyRenderer {
    private static final float[] SUNSET = {0.92f, 0.31f, 0.28f};
    private static final float[] SUNRISE = {0.98f, 0.56f, 0.36f};
    private static final float[] NIGHT = {0.025f, 0.035f, 0.12f};
    private static final float[] STARRY = {0.035f, 0.045f, 0.14f};
    private static final float[] PURPLE = {0.25f, 0.10f, 0.48f};
    private static final float[] DAY = {0.36f, 0.62f, 0.94f};
    private static final float[] DUSK = {0.88f, 0.32f, 0.28f};
    private static final float[] DEEP_NIGHT = {0.045f, 0.055f, 0.16f};
    private static final String[] MODE_NAMES = {
            "Vanilla", "Sunset", "Sunrise", "Night", "Starry", "Purple", "Dynamic"
    };

    private CustomSkyRenderer() {}

    public static String modeName() {
        int mode = clamp(ModConfig.skyMode, 0, MODE_NAMES.length - 1);
        return MODE_NAMES[mode];
    }

    public static Vec3d skyColor(ClientWorld world, float tickDelta, Vec3d vanilla) {
        if (!active(world)) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 6);
        if (mode == 0) return vanilla;

        float day = (float) ((world.getTime() % 24000L + 24000L) % 24000L) / 24000.0f;
        float[] rgb = palette(mode, day);
        float intensity = (float) clamp(ModConfig.skyIntensity, 0.0, 1.0) * qualityFactor();
        float r = mix((float) vanilla.x, rgb[0], intensity);
        float g = mix((float) vanilla.y, rgb[1], intensity);
        float b = mix((float) vanilla.z, rgb[2], intensity);
        return new Vec3d(clamp(r, 0.0f, 1.0f), clamp(g, 0.0f, 1.0f), clamp(b, 0.0f, 1.0f));
    }

    public static Vec3d cloudColor(ClientWorld world, float tickDelta, Vec3d vanilla) {
        if (!active(world) || ModConfig.skyMode == 0) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 6);
        float[] rgb = palette(mode, (float) ((world.getTime() % 24000L + 24000L) % 24000L) / 24000.0f);
        float amount = (float) (clamp(ModConfig.skyIntensity, 0.0, 1.0) * 0.24 * qualityFactor());
        return new Vec3d(mix((float) vanilla.x, rgb[0], amount),
                mix((float) vanilla.y, rgb[1], amount),
                mix((float) vanilla.z, rgb[2], amount));
    }

    public static float starBrightness(ClientWorld world, float vanilla) {
        if (!active(world)) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 6);
        if (mode == 0) return vanilla;
        float target = switch (mode) {
            case 3 -> 0.90f;
            case 4 -> 1.0f;
            case 5 -> 0.82f;
            case 6 -> {
                long time = (world.getTime() % 24000L + 24000L) % 24000L;
                yield time < 12000L ? 0.0f : 0.85f;
            }
            default -> vanilla;
        };
        return mix(vanilla, target, (float) clamp(ModConfig.skyIntensity, 0.0, 1.0) * qualityFactor());
    }

    private static boolean active(ClientWorld world) {
        return ModConfig.customSkyEnabled
                && world != null
                && world.getRegistryKey() == World.OVERWORLD;
    }

    private static float[] palette(int mode, float day) {
        return switch (mode) {
            case 1 -> SUNSET;
            case 2 -> SUNRISE;
            case 3 -> NIGHT;
            case 4 -> STARRY;
            case 5 -> PURPLE;
            case 6 -> dynamicPalette(day);
            default -> DAY;
        };
    }

    private static float[] dynamicPalette(float day) {
        if (day < 0.20f || day >= 0.82f) return DEEP_NIGHT;
        if (day < 0.28f) return SUNRISE;
        if (day < 0.68f) return DAY;
        return DUSK;
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
