package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/**
 * Reusable animated module-card state. Rendering is integer-pixel only and
 * animation time is measured with System.nanoTime(), not frame counts.
 */
public final class ModuleCard {
    private boolean expanded;
    private double progress;
    private long lastNanos = System.nanoTime();

    public boolean isExpanded() { return expanded; }
    public double progress() { return progress; }
    public int animatedContentHeight(int fullHeight) {
        return (int)Math.round(fullHeight * progress);
    }

    public void setExpanded(boolean value) { expanded = value; }
    public void toggle() { expanded = !expanded; }

    public void tick() {
        long now = System.nanoTime();
        double dt = Math.min(0.05, Math.max(0.0, (now - lastNanos) / 1_000_000_000.0));
        lastNanos = now;
        double target = expanded ? 1.0 : 0.0;
        double step = Math.min(1.0, dt / 0.180);
        double eased = 1.0 - Math.pow(1.0 - step, 3.0);
        if (expanded) progress += (target - progress) * eased;
        else progress += (target - progress) * eased;
        if (Math.abs(target - progress) < 0.001) progress = target;
    }

    public void renderHeader(DrawContext c, int x, int y, int width, int accent,
                             boolean enabled, boolean hovered, int mouseX, int mouseY) {
        int bg = hovered ? 0xE52A2634 : 0xD91D1A25;
        c.fill(x, y, x + width, y + 40, bg);
        c.drawBorder(x, y, width, 40, hovered ? ((0x66 << 24) | (accent & 0xFFFFFF)) : 0x66534D60);
        if (enabled) c.fill(x, y + 4, x + 3, y + 36, accent);
        c.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                "Автосбор ягод", x + 12, y + 6, 0xFFF5F2FF, false);
        c.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                "Собирает ягоды в радиусе без поломки кустов", x + 12, y + 22, 0xFFAAA5B8, false);
        int tx = x + width - 66;
        c.fill(tx, y + 12, tx + 32, y + 28, enabled ? accent : 0xFF484451);
        int knob = enabled ? tx + 18 : tx + 2;
        c.fill(knob, y + 14, knob + 12, y + 26, 0xFFF7F4FF);
        int cx = x + width - 20, cy = y + 20;
        if (progress < 0.5) {
            c.drawHorizontalLine(cx - 4, cx, cy - 2, 0xFFD5D0E0);
            c.drawHorizontalLine(cx - 4, cx, cy + 2, 0xFFD5D0E0);
        } else {
            c.drawHorizontalLine(cx - 4, cx, cy - 2, 0xFFD5D0E0);
            c.drawHorizontalLine(cx - 4, cx, cy + 2, 0xFFD5D0E0);
        }
    }
}
