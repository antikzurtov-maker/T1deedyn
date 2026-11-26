package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class T1deedynScreen extends Screen {
    private static final int PANEL = 0xF0181822;
    private static final int PANEL_LIGHT = 0xFF242432;
    private static final int PURPLE = 0xFF8B5CF6;
    private static final int TEXT = 0xFFF4F0FF;
    private static final int MUTED = 0xFFAAA5B8;
    private int left, top, panelWidth, panelHeight;

    public T1deedynScreen() { super(Text.literal("T1deedyn")); }

    @Override
    protected void init() {
        panelWidth = Math.min(520, this.width - 32);
        panelHeight = Math.min(310, this.height - 32);
        left = (this.width - panelWidth) / 2;
        top = (this.height - panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(left, top, left + panelWidth, top + panelHeight, PANEL);
        context.fill(left, top, left + panelWidth, top + 3, PURPLE);
        context.fill(left, top, left + 112, top + panelHeight, 0xFF11111A);
        context.drawText(textRenderer, "T1deedyn", left + 16, top + 17, TEXT, false);
        context.drawText(textRenderer, "VISUAL CLIENT", left + 16, top + 34, MUTED, false);
        String[] tabs = {"Главная", "Визуал", "HUD", "Анимации", "Темы", "Настройки"};
        for (int i = 0; i < tabs.length; i++) {
            int y = top + 66 + i * 31;
            if (i == 0) context.fill(left + 8, y - 5, left + 104, y + 19, 0xFF302644);
            context.drawText(textRenderer, tabs[i], left + 18, y, i == 0 ? TEXT : MUTED, false);
        }
        int x = left + 132;
        context.drawText(textRenderer, "Главная панель", x, top + 23, TEXT, false);
        context.drawText(textRenderer, "Персональная настройка визуального интерфейса", x, top + 42, MUTED, false);
        drawToggle(context, x, top + 86, "Частицы интерфейса", ModConfig.particles);
        drawToggle(context, x, top + 126, "Чистый HUD", ModConfig.cleanHud);
        context.drawText(textRenderer, "Масштаб интерфейса", x, top + 180, TEXT, false);
        context.fill(x, top + 207, left + panelWidth - 28, top + 211, PANEL_LIGHT);
        int knob = x + (int) ((left + panelWidth - 28 - x) * ((ModConfig.uiScale - 0.75) / 0.5));
        context.fill(knob - 4, top + 201, knob + 4, top + 217, PURPLE);
        context.drawText(textRenderer, String.format("%.2fx", ModConfig.uiScale), left + panelWidth - 65, top + 180, MUTED, false);
        context.drawText(textRenderer, "X  закрыть меню", x, top + panelHeight - 25, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext c, int x, int y, String label, boolean enabled) {
        c.drawText(textRenderer, label, x, y, TEXT, false);
        int bx = left + panelWidth - 66;
        c.fill(bx, y - 4, bx + 38, y + 12, enabled ? 0xFF6844B8 : PANEL_LIGHT);
        c.fill(enabled ? bx + 22 : bx + 3, y - 1, enabled ? bx + 34 : bx + 15, y + 9, TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = left + 132;
        if (button == 0 && mouseX >= left + panelWidth - 70 && mouseX <= left + panelWidth - 20) {
            if (mouseY >= top + 82 && mouseY <= top + 106) { ModConfig.particles = !ModConfig.particles; ModConfig.save(); return true; }
            if (mouseY >= top + 122 && mouseY <= top + 146) { ModConfig.cleanHud = !ModConfig.cleanHud; ModConfig.save(); return true; }
        }
        if (button == 0 && mouseY >= top + 196 && mouseY <= top + 222 && mouseX >= x && mouseX <= left + panelWidth - 28) {
            ModConfig.uiScale = 0.75 + 0.5 * ((mouseX - x) / (double)(left + panelWidth - 28 - x));
            ModConfig.save(); return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
