package dev.t1deedyn;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Lightweight palette controller. It changes the existing vanilla sky/cloud colours
 * and star brightness; it does not replace WorldRenderer or allocate geometry.
 */
public final class CustomSkyRenderer {
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
        float intensity = (float) clamp(ModConfig.skyIntensity, 0.0, 1.0);
        float r = mix((float) vanilla.x, rgb[0], intensity);
        float g = mix((float) vanilla.y, rgb[1], intensity);
        float b = mix((float) vanilla.z, rgb[2], intensity);
        return new Vec3d(clamp(r, 0.0f, 1.0f), clamp(g, 0.0f, 1.0f), clamp(b, 0.0f, 1.0f));
    }

    public static Vec3d cloudColor(ClientWorld world, float tickDelta, Vec3d vanilla) {
        if (!active(world) || ModConfig.skyMode == 0) return vanilla;
        int mode = clamp(ModConfig.skyMode, 0, 6);
        float[] rgb = palette(mode, (float) ((world.getTime() % 24000L + 24000L) % 24000L) / 24000.0f);
        float amount = (float) (clamp(ModConfig.skyIntensity, 0.0, 1.0) * 0.24);
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
        return mix(vanilla, target, (float) clamp(ModConfig.skyIntensity, 0.0, 1.0));
    }

    private static boolean active(ClientWorld world) {
        return ModConfig.customSkyEnabled
                && world != null
                && world.getRegistryKey() == World.OVERWORLD;
    }

    private static float[] palette(int mode, float day) {
        return switch (mode) {
            case 1 -> new float[]{0.92f, 0.31f, 0.28f};
            case 2 -> new float[]{0.98f, 0.56f, 0.36f};
            case 3 -> new float[]{0.025f, 0.035f, 0.12f};
            case 4 -> new float[]{0.035f, 0.045f, 0.14f};
            case 5 -> new float[]{0.25f, 0.10f, 0.48f};
            case 6 -> dynamicPalette(day);
            default -> new float[]{0.45f, 0.62f, 0.95f};
        };
    }

    private static float[] dynamicPalette(float day) {
        if (day < 0.20f || day >= 0.82f) return new float[]{0.045f, 0.055f, 0.16f};
        if (day < 0.28f) return new float[]{0.95f, 0.42f, 0.28f};
        if (day < 0.68f) return new float[]{0.36f, 0.62f, 0.94f};
        return new float[]{0.88f, 0.32f, 0.28f};
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
