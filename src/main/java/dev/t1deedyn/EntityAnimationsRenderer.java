package dev.t1deedyn;

/**
 * Deterministic additive animation curves. The model mixin reuses these values
 * after vanilla has prepared its pose; no per-frame collections or objects.
 */
public final class EntityAnimationsRenderer {
    private static final String[] MODE_NAMES = {"Smooth", "Classic", "Dynamic"};

    private EntityAnimationsRenderer() {}

    public static String modeName() {
        return MODE_NAMES[Math.max(0, Math.min(2, ModConfig.entityAnimationMode))];
    }

    public static float intensity() {
        return (float) Math.max(0.0, Math.min(1.0, ModConfig.entityAnimationIntensity));
    }

    public static float idleSway(float age) {
        return (float) Math.sin(age * 0.075f) * 0.025f * intensity();
    }

    public static float bodyRoll(float age, float limbDistance, boolean sneaking, boolean sprinting) {
        float amount = intensity();
        float walk = clamp(limbDistance);
        float speed = ModConfig.entityAnimationMode == 1 ? 0.16f
                : ModConfig.entityAnimationMode == 2 ? 0.25f : 0.19f;
        float idle = (float) Math.sin(age * 0.075f) * 0.018f;
        float stride = (float) Math.sin(age * speed) * (sprinting ? 0.075f : 0.052f) * walk;
        return (idle + stride) * amount * (sneaking ? 0.5f : 1.0f);
    }

    public static float armRoll(float age, float limbDistance, boolean right, boolean sprinting) {
        float walk = clamp(limbDistance);
        float direction = right ? 1.0f : -1.0f;
        float speed = ModConfig.entityAnimationMode == 1 ? 0.16f
                : ModConfig.entityAnimationMode == 2 ? 0.25f : 0.20f;
        float walking = (float) Math.cos(age * speed) * (sprinting ? 0.12f : 0.075f) * walk;
        float idle = (float) Math.sin(age * 0.07f + (right ? 0.0f : 1.2f)) * 0.018f;
        return direction * (walking + idle) * intensity();
    }

    public static float headRoll(float age, float limbDistance) {
        float walk = clamp(limbDistance);
        return ((float) Math.sin(age * 0.06f) * 0.012f
                + (float) Math.sin(age * 0.18f) * 0.008f * walk) * intensity();
    }

    public static float jumpAmount(double velocityY, boolean onGround) {
        if (onGround) return 0.0f;
        return (float) Math.max(0.0, Math.min(1.0, Math.abs(velocityY) * 1.7));
    }

    public static float swingAmount(float swingProgress) {
        return (float) Math.sin(Math.max(0.0f, Math.min(1.0f, swingProgress)) * Math.PI);
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
