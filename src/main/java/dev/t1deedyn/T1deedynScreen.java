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
    private static final String[] TABS = {"Главная", "Визуал", "HUD", "Анимации", "Темы", "Настройки"};
    private int left, top, panelWidth, panelHeight, selectedTab;
    private boolean dragging;
    private double dragOffsetX, dragOffsetY;
    private boolean sliderDragging;
    private float openProgress;

    public T1deedynScreen() { super(Text.literal("T1deedyn")); }

    @Override
    protected void init() {
        panelWidth = Math.min(560, this.width - 24);
        panelHeight = Math.min(340, this.height - 24);
        left = (this.width - panelWidth) / 2;
        top = (this.height - panelHeight) / 2;
    }

    @Override
    public void tick() {
        if (openProgress < 1.0f) openProgress = Math.min(1.0f, openProgress + 0.12f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int slide = ModConfig.menuAnimations ? (int)((1.0f - openProgress) * 12.0f) : 0;
        int yTop = top + slide;
        context.fill(left, yTop, left + panelWidth, yTop + panelHeight, PANEL);
        context.fill(left, yTop, left + panelWidth, yTop + 3, PURPLE);
        context.fill(left, yTop, left + 126, yTop + panelHeight, 0xFF11111A);
        context.fill(left + 126, yTop + 3, left + 127, yTop + panelHeight, 0xFF302B3B);

        context.drawText(textRenderer, "T1deedyn", left + 16, yTop + 17, TEXT, false);
        context.drawText(textRenderer, "VISUAL CLIENT", left + 16, yTop + 35, MUTED, false);
        context.fill(left + 14, yTop + 53, left + 110, yTop + 54, 0xFF34303F);

        for (int i = 0; i < TABS.length; i++) {
            int y = yTop + 68 + i * 34;
            boolean active = selectedTab == i;
            if (active) {
                context.fill(left + 8, y - 6, left + 118, y + 20, 0xFF302644);
                context.fill(left + 8, y - 6, left + 11, y + 20, PURPLE);
            }
            context.drawText(textRenderer, TABS[i], left + 20, y, active ? TEXT : MUTED, false);
        }

        int x = left + 148;
        String heading = switch (selectedTab) {
            case 1 -> "Визуальные эффекты";
            case 2 -> "Элементы HUD";
            case 3 -> "Анимации интерфейса";
            case 4 -> "Оформление";
            case 5 -> "Настройки клиента";
            default -> "Главная панель";
        };
        context.drawText(textRenderer, heading, x, yTop + 22, TEXT, false);
        context.drawText(textRenderer, "Настрой параметры интерфейса T1deedyn", x, yTop + 42, MUTED, false);

        switch (selectedTab) {
            case 1 -> {
                drawToggle(context, x, yTop + 86, "Частицы интерфейса", ModConfig.particles);
                drawToggle(context, x, yTop + 126, "Уменьшенный огонь", ModConfig.lowFire);
                drawToggle(context, x, yTop + 166, "Плавная камера", ModConfig.smoothCamera);
            }
            case 2 -> {
                drawToggle(context, x, yTop + 86, "Чистый HUD", ModConfig.cleanHud);
                drawToggle(context, x, yTop + 126, "Координаты", ModConfig.showCoordinates);
            }
            case 3 -> drawToggle(context, x, yTop + 86, "Анимация открытия меню", ModConfig.menuAnimations);
            case 4 -> {
                context.fill(x, yTop + 86, x + 42, yTop + 112, PURPLE);
                context.fill(x + 52, yTop + 86, x + 94, yTop + 112, 0xFF5264B8);
                context.fill(x + 104, yTop + 86, x + 146, yTop + 112, 0xFF299B83);
                context.drawText(textRenderer, "Акцентные цвета (выбор темы появится позже)", x, yTop + 126, MUTED, false);
            }
            case 5 -> {
                context.drawText(textRenderer, "Клавиша меню: X", x, yTop + 86, TEXT, false);
                context.drawText(textRenderer, "Файл настроек: config/t1deedyn.properties", x, yTop + 112, MUTED, false);
                context.drawText(textRenderer, "Настройки сохраняются автоматически", x, yTop + 138, MUTED, false);
            }
            default -> {
                drawToggle(context, x, yTop + 86, "Частицы интерфейса", ModConfig.particles);
                drawToggle(context, x, yTop + 126, "Чистый HUD", ModConfig.cleanHud);
                context.drawText(textRenderer, "Масштаб интерфейса", x, yTop + 180, TEXT, false);
                drawSlider(context, x, yTop + 210);
            }
        }

        context.drawText(textRenderer, "T1deedyn  •  build 0.2.0", x, yTop + panelHeight - 24, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext c, int x, int y, String label, boolean enabled) {
        c.drawText(textRenderer, label, x, y, TEXT, false);
        int bx = left + panelWidth - 72;
        c.fill(bx, y - 4, bx + 42, y + 13, enabled ? 0xFF6844B8 : PANEL_LIGHT);
        c.fill(enabled ? bx + 25 : bx + 4, y - 1, enabled ? bx + 37 : bx + 16, y + 10, TEXT);
    }

    private void drawSlider(DrawContext c, int x, int y) {
        int end = left + panelWidth - 34;
        c.fill(x, y, end, y + 4, PANEL_LIGHT);
        int knob = x + (int)((end - x) * ((ModConfig.uiScale - 0.75) / 0.5));
        c.fill(knob - 5, y - 5, knob + 5, y + 9, PURPLE);
        c.drawText(textRenderer, String.format("%.2fx", ModConfig.uiScale), end - 38, y - 25, MUTED, false);
    }

    private void setSlider(double mouseX) {
        int x = left + 148;
        int end = left + panelWidth - 34;
        ModConfig.uiScale = Math.max(0.75, Math.min(1.25, 0.75 + 0.5 * ((mouseX - x) / (double)(end - x))));
        ModConfig.save();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= top && mouseY <= top + 48 && mouseX >= left && mouseX <= left + panelWidth) {
            dragging = true;
            dragOffsetX = mouseX - left;
            dragOffsetY = mouseY - top;
            return true;
        }
        if (button == 0 && mouseX >= left + 8 && mouseX <= left + 120) {
            for (int i = 0; i < TABS.length; i++) {
                int y = top + 62 + i * 34;
                if (mouseY >= y && mouseY <= y + 27) { selectedTab = i; return true; }
            }
        }
        int x = left + 148;
        int bx = left + panelWidth - 72;
        if (button == 0 && mouseX >= bx - 2 && mouseX <= bx + 46) {
            if (selectedTab == 0) {
                if (mouseY >= top + 82 && mouseY <= top + 106) { ModConfig.particles = !ModConfig.particles; ModConfig.save(); return true; }
                if (mouseY >= top + 122 && mouseY <= top + 146) { ModConfig.cleanHud = !ModConfig.cleanHud; ModConfig.save(); return true; }
            } else if (selectedTab == 1) {
                if (mouseY >= top + 82 && mouseY <= top + 106) { ModConfig.particles = !ModConfig.particles; ModConfig.save(); return true; }
                if (mouseY >= top + 122 && mouseY <= top + 146) { ModConfig.lowFire = !ModConfig.lowFire; ModConfig.save(); return true; }
                if (mouseY >= top + 162 && mouseY <= top + 186) { ModConfig.smoothCamera = !ModConfig.smoothCamera; ModConfig.save(); return true; }
            } else if (selectedTab == 2) {
                if (mouseY >= top + 82 && mouseY <= top + 106) { ModConfig.cleanHud = !ModConfig.cleanHud; ModConfig.save(); return true; }
                if (mouseY >= top + 122 && mouseY <= top + 146) { ModConfig.showCoordinates = !ModConfig.showCoordinates; ModConfig.save(); return true; }
            } else if (selectedTab == 3 && mouseY >= top + 82 && mouseY <= top + 106) {
                ModConfig.menuAnimations = !ModConfig.menuAnimations; ModConfig.save(); return true;
            }
        }
        if (button == 0 && selectedTab == 0 && mouseY >= top + 198 && mouseY <= top + 224 && mouseX >= x && mouseX <= left + panelWidth - 34) {
            sliderDragging = true;
            setSlider(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            left = Math.max(0, Math.min(this.width - panelWidth, (int)(mouseX - dragOffsetX)));
            top = Math.max(0, Math.min(this.height - panelHeight, (int)(mouseY - dragOffsetY)));
            return true;
        }
        if (sliderDragging && button == 0) { setSlider(mouseX); return true; }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { dragging = false; sliderDragging = false; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
