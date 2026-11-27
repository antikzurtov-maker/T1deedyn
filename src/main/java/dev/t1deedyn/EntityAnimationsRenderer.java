package dev.t1deedyn;

/**
 * Computes additive, deterministic model offsets. No per-frame objects are created.
 * The mixin applies these values after vanilla has reset the player model pose.
 */
public final class EntityAnimationsRenderer {
    private static final String[] MODE_NAMES = {"Smooth", "Classic", "Dynamic"};

    private EntityAnimationsRenderer() {}

    public static String modeName() {
        int mode = Math.max(0, Math.min(2, ModConfig.entityAnimationMode));
        return MODE_NAMES[mode];
    }

    public static float intensity() {
        return (float) Math.max(0.0, Math.min(1.0, ModConfig.entityAnimationIntensity));
    }

    public static float idleSway(float age) {
        return (float) Math.sin(age * 0.075f) * 0.025f * intensity();
    }

    public static float bodyRoll(float age, float limbDistance, boolean sneaking) {
        float amount = intensity();
        float walk = Math.min(1.0f, Math.max(0.0f, limbDistance));
        float idle = (float) Math.sin(age * 0.075f) * 0.018f;
        float stride = (float) Math.sin(age * (ModConfig.entityAnimationMode == 2 ? 0.24f : 0.18f)) * 0.055f * walk;
        float scale = sneaking ? 0.55f : 1.0f;
        return (idle + stride) * amount * scale;
    }

    public static float armRoll(float age, float limbDistance, boolean right) {
        float walk = Math.min(1.0f, Math.max(0.0f, limbDistance));
        float direction = right ? 1.0f : -1.0f;
        float speed = ModConfig.entityAnimationMode == 1 ? 0.16f : 0.21f;
        float walking = (float) Math.cos(age * speed) * 0.075f * walk;
        float idle = (float) Math.sin(age * 0.07f + (right ? 0.0f : 1.2f)) * 0.018f;
        return direction * (walking + idle) * intensity();
    }

    public static float headRoll(float age, float limbDistance) {
        float walk = Math.min(1.0f, Math.max(0.0f, limbDistance));
        return (float) Math.sin(age * 0.06f) * 0.012f * intensity()
                + (float) Math.sin(age * 0.18f) * 0.008f * walk * intensity();
    }
}
