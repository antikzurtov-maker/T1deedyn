package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class T1deedynScreen extends Screen {
    private static final int TEXT = 0xFFF5F2FF;
    private static final int MUTED = 0xFFAAA5B8;
    private static final int SIDEBAR = 0xFF15131D;
    private static final String[] TABS = {"Главная", "Визуал", "HUD", "Игра", "Анимации", "Темы", "Настройки"};
    private static final int[] ACCENTS = {0xFF9B70FF, 0xFF62A0FF, 0xFF54D6B0};
    private static final String[] THEME_NAMES = {"Violet", "Ocean", "Mint"};

    private int left, top, panelWidth, panelHeight, selectedTab;
    private boolean dragging, sliderDragging;
    private double dragOffsetX, dragOffsetY;
    private int activeSlider = -1;
    private float openProgress;
        private final ScrollPanel scrollPanel = new ScrollPanel();
    private long lastFrameNanos = System.nanoTime();
    private boolean contentDragConsumed;
    private Integer previousMenuBlur;

    public T1deedynScreen() { super(Text.literal("T1deedyn")); }

    private int accent() {
        if (ModConfig.rgbLighting) {
            float hue = (System.currentTimeMillis() % 6000L) / 6000.0f;
            return java.awt.Color.HSBtoRGB(hue, 0.72f, 1.0f) | 0xFF000000;
        }
        int rgb = Integer.parseInt(ModConfig.accentHex.substring(1), 16);
        float brightness = (float) ModConfig.accentBrightness;
        int r = Math.min(255, Math.round(((rgb >> 16) & 255) * brightness));
        int g = Math.min(255, Math.round(((rgb >> 8) & 255) * brightness));
        int b = Math.min(255, Math.round((rgb & 255) * brightness));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int panelColor() {
        int a = (int)(Math.max(0.72, Math.min(1.0, ModConfig.menuOpacity)) * 255);
        return (a << 24) | 0x001B1925;
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (previousMenuBlur == null && mc.options != null) {
            previousMenuBlur = mc.options.getMenuBackgroundBlurrinessValue();
        }
        if (mc.options != null) mc.options.getMenuBackgroundBlurriness().setValue(0);

        // GUI coordinates are already integer-scaled by Minecraft. Keep the
        // menu geometry in whole GUI pixels and never apply fractional matrices.
        panelWidth = Math.max(1, Math.min(610, this.width - 24));
        panelHeight = Math.max(1, Math.min(370, this.height - 24));
        left = Math.round((this.width - panelWidth) / 2.0f);
        top = Math.round((this.height - panelHeight) / 2.0f);
    }

    @Override public void tick() {
        if (openProgress < 1.0f) openProgress = Math.min(1.0f, openProgress + 0.16f);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_X || keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (scrollPanel.key(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        int alpha = Math.round((float)(Math.max(0.0, Math.min(0.75, ModConfig.backgroundDim)) * 255.0));
        // A single flat black overlay; no texture, shader, framebuffer or blur.
        context.fill(0, 0, width, height, (alpha << 24));
    }

    @Override
    protected void applyBlur(float delta) {
        // Intentionally disabled for this screen.
    }

    @Override
    public void removed() {
        if (previousMenuBlur != null && client != null && client.options != null) {
            client.options.getMenuBackgroundBlurriness().setValue(previousMenuBlur);
            previousMenuBlur = null;
        }
        super.removed();
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        renderBackground(c, mouseX, mouseY, delta);
        int slide = ModConfig.menuAnimations ? Math.round((1.0f - openProgress) * 10.0f) : 0;
        int py = top + slide;
        if (ModConfig.skyVisual) drawSky(c, py);
        rounded(c, left, py, left + panelWidth, py + panelHeight, 13, panelColor());
        if (ModConfig.rgbLighting) {
            int glow = accent();
            c.fill(left + 12, py + 5, left + panelWidth - 12, py + 7, glow);
            c.fill(left + 5, py + 13, left + 7, py + panelHeight - 13, glow);
            c.fill(left + panelWidth - 7, py + 13, left + panelWidth - 5, py + panelHeight - 13, glow);
        }
        rounded(c, left + 1, py + 1, left + panelWidth - 1, py + 4, 2, accent());
        rounded(c, left + 1, py + 5, left + 137, py + panelHeight - 1, 10, SIDEBAR);
        c.fill(left + 136, py + 12, left + 137, py + panelHeight - 12, 0xFF393344);

        c.drawText(textRenderer, "T1deedyn", left + 19, py + 17, TEXT, false);
        c.drawText(textRenderer, "VISUAL CLIENT", left + 19, py + 34, MUTED, false);
        c.fill(left + 17, py + 54, left + 117, py + 55, 0xFF393344);

        for (int i = 0; i < TABS.length; i++) {
            int y = py + 70 + i * 36;
            if (selectedTab == i) {
                rounded(c, left + 9, y - 7, left + 126, y + 21, 7, 0xFF302641);
                rounded(c, left + 9, y - 7, left + 12, y + 21, 2, accent());
            }
            c.drawText(textRenderer, TABS[i], left + 22, y, selectedTab == i ? TEXT : MUTED, false);
        }

        int x = left + 158;
        int contentRight = left + panelWidth - 22;
        int viewportY = py + 66;
        int viewportH = Math.max(32, panelHeight - 104);
        int contentH = switch (selectedTab) { case 1 -> 230; case 2 -> 170; case 3 -> 69 + 20 * 22 + 20; case 4 -> 150; case 5 -> 220; case 6 -> 300; default -> 230; };
        scrollPanel.layout(selectedTab, x - 8, viewportY, contentRight - x + 16, viewportH, contentH);
        long frameNow = System.nanoTime();
        scrollPanel.tick(Math.min(0.1, (frameNow - lastFrameNanos) / 1_000_000_000.0));
        lastFrameNanos = frameNow;
        String heading = switch (selectedTab) {
            case 1 -> "Визуальные эффекты";
            case 2 -> "Настройка HUD";
            case 3 -> "Игра / AutoBerry";
            case 4 -> "Анимации";
            case 5 -> "Оформление меню";
            case 6 -> "Настройки интерфейса";
            default -> "Главная панель";
        };
        c.drawText(textRenderer, heading, x, py + 22, TEXT, false);
        c.drawText(textRenderer, "Персонализируй внешний вид T1deedyn", x, py + 42, MUTED, false);

        c.enableScissor(x - 8, viewportY, contentRight + 8, viewportY + viewportH);
        c.getMatrices().push();
        c.getMatrices().translate(0.0, -scrollPanel.offset(), 0.0);
        switch (selectedTab) {
            case 1 -> {
                toggle(c, x, py + 88, contentRight, "Частицы в меню", ModConfig.particles);
                c.drawText(textRenderer, "Декоративные светящиеся точки на фоне меню", x, py + 108, MUTED, false);
                toggle(c, x, py + 148, contentRight, "Уменьшенный огонь", ModConfig.lowFire);
                toggle(c, x, py + 188, contentRight, "Плавная камера", ModConfig.smoothCamera);
                toggle(c, x, py + 228, contentRight, "Ночное небо / звёзды", ModConfig.skyVisual);
                toggle(c, x, py + 268, contentRight, "RGB-подсветка", ModConfig.rgbLighting);
                }
            case 2 -> {
                toggle(c, x, py + 88, contentRight, "Чистый HUD", ModConfig.cleanHud);
                toggle(c, x, py + 132, contentRight, "Показывать координаты", ModConfig.showCoordinates);
                c.drawText(textRenderer, "Переключатели HUD пока сохраняют настройки.", x, py + 190, MUTED, false);
            }
            case 3 -> drawGameSettings(c, x, py, contentRight, mouseX, mouseY);
            case 4 -> {
                toggle(c, x, py + 88, contentRight, "Анимация открытия", ModConfig.menuAnimations);
                c.drawText(textRenderer, "Плавное появление и смещение панели.", x, py + 111, MUTED, false);
            }
            case 5 -> {
                c.drawText(textRenderer, "Цветовая тема", x, py + 88, TEXT, false);
                rounded(c, x, py + 116, x + 48, py + 151, 8, ACCENTS[0]);
                rounded(c, x + 59, py + 116, x + 107, py + 151, 8, ACCENTS[1]);
                rounded(c, x + 118, py + 116, x + 166, py + 151, 8, ACCENTS[2]);
                c.drawText(textRenderer, "Активная: " + THEME_NAMES[ModConfig.accentTheme], x, py + 164, MUTED, false);
                c.drawText(textRenderer, "Нажми на цвет, чтобы применить тему.", x, py + 190, MUTED, false);
            }
            case 6 -> {
                c.drawText(textRenderer, "Масштаб меню", x, py + 82, TEXT, false);
                c.drawText(textRenderer, "1x (целый масштаб)", x, py + 108, MUTED, false);
                c.drawText(textRenderer, "Непрозрачность панели", x, py + 151, TEXT, false);
                slider(c, x, py + 177, contentRight, ModConfig.menuOpacity, 0.72, 1.0, 1);
                c.drawText(textRenderer, "Затемнение мира", x, py + 220, TEXT, false);
                slider(c, x, py + 246, contentRight, ModConfig.backgroundDim, 0.0, 0.75, 2);
                c.drawText(textRenderer, "X или ESC  •  закрыть меню", x, py + panelHeight - 28, MUTED, false);
            }
            default -> {
                c.drawText(textRenderer, "Добро пожаловать!", x, py + 88, TEXT, false);
                c.drawText(textRenderer, "Меню открывается и закрывается клавишей X.", x, py + 112, MUTED, false);
                c.drawText(textRenderer, "Настрой внешний вид во вкладке «Настройки».", x, py + 136, MUTED, false);
                rounded(c, x, py + 84, contentRight, py + 140, 9, Theme.get(accent()).panelTint);
                c.drawBorder(x, py + 84, contentRight - x, 56, Theme.get(accent()).accentDim);
                c.drawText(textRenderer, "СОЗДАТЕЛИ", x + 12, py + 91, MUTED, false);
                rounded(c, x + 12, py + 108, x + 30, py + 126, 4, accent());
                c.drawText(textRenderer, "T1deedyn", x + 38, py + 111, Theme.get(accent()).accentBright, false);
                c.drawText(textRenderer, "&", x + 108, py + 111, 0x99AAA5B8, false);
                c.drawText(textRenderer, "PvE_BoTiK", x + 123, py + 111, Theme.get(accent()).accentBright, false);
                rounded(c, x, py + 152, x + 205, py + 198, 9, 0xFF282333);
                c.drawText(textRenderer, "T1  •  CUSTOM VISUALS", x + 14, py + 169, accent(), false);
            }
        }

        c.getMatrices().pop();
        c.disableScissor();
        scrollPanel.renderBar(c, accent());
        if (selectedTab == 6) {
            c.drawText(textRenderer, "X или ESC — закрыть меню", x, py + panelHeight - 31, MUTED, false);
            c.drawText(textRenderer, "T1deedyn  •  build " + ModConfig.BUILD_VERSION, x, py + panelHeight - 16, MUTED, false);
        }
        if (selectedTab == 1 && ModConfig.particles) drawParticles(c, mouseX, mouseY);
        // Do not call Screen.render here: this screen owns its complete
        // rendering pass and must not trigger any inherited background path.
    }


    private void drawGameSettings(DrawContext c, int x, int py, int right, int mouseX, int mouseY) {
        c.drawText(textRenderer, "AutoBerry  •  V — вкл/выкл", x, py + 51, MUTED, false);
        for (int i = 0; i < 20; i++) {
            int index = i;
            int y = py + 69 + i * 22;
            if ((i & 1) == 0) c.fill(x - 5, y - 3, right, y + 17, 0x331F1B2A);
            c.drawText(textRenderer, gameSettingName(index), x, y, TEXT, false);
            String value = gameSettingValue(index);
            c.drawText(textRenderer, value, right - textRenderer.getWidth(value), y, accent(), false);
        }
        int hovered = (int)((mouseY - (py + 69)) / 22.0) + scrollPanel.offset() / 22;
        String hint = mouseX >= x && mouseX <= right && hovered >= 0 && hovered < 20 ? gameSettingHint(hovered) : "Колесо мыши: прокрутка  •  нажми строку для изменения";
        c.drawText(textRenderer, hint, x, py + panelHeight - 28, MUTED, false);
    }
    private String gameSettingHint(int i) {
        return switch (i) {
            case 0 -> "Автосбор включается отдельно; V переключает функцию.";
            case 1 -> "Радиус от глаз игрока: 1.0–5.0 блока, шаг 0.5.";
            case 2 -> "Радиус ограничивается атрибутом дальности + небольшой запас.";
            case 3 -> "Собирать обычные кусты сладких ягод.";
            case 4 -> "Собирать светящиеся пещерные лозы с ягодами.";
            case 5 -> "Вторая стадия даёт меньше ягод, чем полностью спелый куст.";
            case 6 -> "Интервал между кликами: 0 = каждый игровой тик.";
            case 7 -> "К базовой задержке добавляется случайное число от 0 до N.";
            case 8 -> "Предел до 20 кликов/с, ограничен одним действием за тик.";
            case 9 -> "Частота поиска: 1 тик для быстрой фермы.";
            case 10 -> "Пока игрок движется, клики и поиск целей приостанавливаются.";
            case 11 -> "Пауза в инвентаре, чате и любом другом открытом экране.";
            case 12 -> "Не собирать во время еды, натягивания лука или блокирования.";
            case 13 -> "Не кликает с костной мукой, блоками, ведрами и метаемыми предметами.";
            case 14 -> "Проигрывать обычную анимацию взмаха главной руки.";
            case 15 -> "Ванильный звук сбора остаётся; настройка для доп. звуков мода.";
            case 16 -> "Добавлять частицу на месте успешно обработанного куста.";
            case 17 -> "Показывать счётчик собранных кустов на HUD.";
            case 18 -> "Редкая частица рядом с созревшими кустами, без шейдеров.";
            default -> "Проверять лучом, что между глазами и кустом нет блоков.";
        };
    }
    private String gameSettingName(int i) {
        return switch (i) {
            case 0 -> "Включено"; case 1 -> "Радиус"; case 2 -> "Ограничить дальностью";
            case 3 -> "Сладкие ягоды"; case 4 -> "Светящиеся ягоды"; case 5 -> "Минимальная стадия";
            case 6 -> "Задержка клика"; case 7 -> "Случайность задержки"; case 8 -> "Макс. кликов/сек";
            case 9 -> "Частота сканирования"; case 10 -> "Только стоя на месте";
            case 11 -> "Пауза при открытом экране"; case 12 -> "Пауза при использовании";
            case 13 -> "Безопасный режим руки"; case 14 -> "Взмах руки"; case 15 -> "Доп. звук";
            case 16 -> "Частицы сбора"; case 17 -> "Счётчик HUD"; case 18 -> "Подсветка кустов";
            default -> "Только видимые цели";
        };
    }
    private String gameSettingValue(int i) {
        return switch (i) {
            case 0 -> onOff(ModConfig.autoBerryEnabled); case 1 -> String.format(java.util.Locale.ROOT, "%.1f", ModConfig.autoBerryRadius);
            case 2 -> onOff(ModConfig.autoBerryLimitReach); case 3 -> onOff(ModConfig.autoBerrySweet);
            case 4 -> onOff(ModConfig.autoBerryGlow); case 5 -> ModConfig.autoBerryStageTwo ? "С 2-й стадии" : "Только спелые (3)";
            case 6 -> ModConfig.autoBerryClickDelay + " т."; case 7 -> ModConfig.autoBerryRandomDelay + " т.";
            case 8 -> Integer.toString(ModConfig.autoBerryMaxCps); case 9 -> ModConfig.autoBerryScanTicks + " т.";
            case 10 -> onOff(ModConfig.autoBerryOnlyStationary); case 11 -> onOff(ModConfig.autoBerryPauseWithScreen);
            case 12 -> onOff(ModConfig.autoBerryPauseUsingItem); case 13 -> onOff(ModConfig.autoBerrySafeHand);
            case 14 -> onOff(ModConfig.autoBerrySwing); case 15 -> onOff(ModConfig.autoBerrySound);
            case 16 -> onOff(ModConfig.autoBerryParticles); case 17 -> onOff(ModConfig.autoBerryCountHud);
            case 18 -> onOff(ModConfig.autoBerryHighlight); default -> onOff(ModConfig.autoBerryVisibleOnly);
        };
    }
    private String onOff(boolean value) { return value ? "ВКЛ" : "ВЫКЛ"; }
    private void changeGameSetting(int i) {
        switch (i) {
            case 0 -> AutoBerry.toggle(); case 1 -> ModConfig.autoBerryRadius = ModConfig.autoBerryRadius >= 5.0 ? 1.0 : ModConfig.autoBerryRadius + 0.5;
            case 2 -> ModConfig.autoBerryLimitReach = !ModConfig.autoBerryLimitReach;
            case 3 -> ModConfig.autoBerrySweet = !ModConfig.autoBerrySweet; case 4 -> ModConfig.autoBerryGlow = !ModConfig.autoBerryGlow;
            case 5 -> ModConfig.autoBerryStageTwo = !ModConfig.autoBerryStageTwo;
            case 6 -> ModConfig.autoBerryClickDelay = cycle(ModConfig.autoBerryClickDelay, 0, 20);
            case 7 -> ModConfig.autoBerryRandomDelay = cycle(ModConfig.autoBerryRandomDelay, 0, 5);
            case 8 -> ModConfig.autoBerryMaxCps = cycle(ModConfig.autoBerryMaxCps, 1, 20);
            case 9 -> ModConfig.autoBerryScanTicks = cycle(ModConfig.autoBerryScanTicks, 1, 20);
            case 10 -> ModConfig.autoBerryOnlyStationary = !ModConfig.autoBerryOnlyStationary;
            case 11 -> ModConfig.autoBerryPauseWithScreen = !ModConfig.autoBerryPauseWithScreen;
            case 12 -> ModConfig.autoBerryPauseUsingItem = !ModConfig.autoBerryPauseUsingItem;
            case 13 -> ModConfig.autoBerrySafeHand = !ModConfig.autoBerrySafeHand;
            case 14 -> ModConfig.autoBerrySwing = !ModConfig.autoBerrySwing; case 15 -> ModConfig.autoBerrySound = !ModConfig.autoBerrySound;
            case 16 -> ModConfig.autoBerryParticles = !ModConfig.autoBerryParticles; case 17 -> ModConfig.autoBerryCountHud = !ModConfig.autoBerryCountHud;
            case 18 -> ModConfig.autoBerryHighlight = !ModConfig.autoBerryHighlight; case 19 -> ModConfig.autoBerryVisibleOnly = !ModConfig.autoBerryVisibleOnly;
        }
        ModConfig.save();
    }
    private int cycle(int value, int min, int max) { return value >= max ? min : value + 1; }

    private void drawSky(DrawContext c, int py) {
        int x1 = left + 139, x2 = left + panelWidth - 5;
        int y1 = py + 5, y2 = py + panelHeight - 5;
        c.fill(x1, y1, x2, y2, 0xFF101629);
        c.fill(x1, y1, x2, y1 + (y2-y1)/2, 0x331B2C54);
        long t = System.currentTimeMillis() / 180L;
        for (int i=0;i<34;i++) {
            int sx=x1+12+(i*53)%(Math.max(1,x2-x1-24));
            int sy=y1+12+(i*31)%(Math.max(1,y2-y1-24));
            int a=120+(int)((Math.sin((t+i)*0.12)+1)*60);
            c.fill(sx,sy,sx+2,sy+2,(a<<24)|0x00DDE8FF);
        }
    }

    private void drawParticles(DrawContext c, int mx, int my) {
        long t = System.currentTimeMillis() / 35L;
        for (int i = 0; i < 14; i++) {
            int px = left + 150 + (int)((i * 47L + t * (i % 3 + 1)) % Math.max(1, panelWidth - 175));
            int py = top + 60 + (int)((i * 29L + t / (i % 3 + 1)) % Math.max(1, panelHeight - 90));
            c.fill(px, py, px + 2, py + 2, 0x887F63C8);
        }
    }

    private void toggle(DrawContext c, int x, int y, int right, String label, boolean on) {
        c.drawText(textRenderer, label, x, y, TEXT, false);
        int bx = right - 45;
        rounded(c, bx, y - 5, bx + 42, y + 13, 9, on ? accent() : 0xFF3A3744);
        rounded(c, on ? bx + 25 : bx + 4, y - 2, on ? bx + 37 : bx + 16, y + 10, 6, 0xFFF7F4FF);
    }

    private void slider(DrawContext c, int x, int y, int right, double value, double min, double max, int id) {
        int end = right - 2;
        rounded(c, x, y, end, y + 5, 3, 0xFF403B4A);
        int knob = x + (int)((end - x) * ((value - min) / (max - min)));
        rounded(c, x, y, knob, y + 5, 3, accent());
        rounded(c, knob - 5, y - 4, knob + 5, y + 10, 6, 0xFFF5F0FF);
        String valueText = String.format(java.util.Locale.ROOT, "%.2f", value);
        c.drawText(textRenderer, valueText, end - 36, y - 19, MUTED, false);
    }

    private void rounded(DrawContext c, int x1, int y1, int x2, int y2, int r, int color) {
        int radius = Math.max(0, Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        for (int y = y1; y < y2; y++) {
            int inset = 0;
            if (y < y1 + radius) {
                int dy = radius - (y - y1) - 1;
                inset = radius - (int)Math.sqrt(Math.max(0, radius * radius - dy * dy));
            } else if (y >= y2 - radius) {
                int dy = radius - (y2 - y - 1) - 1;
                inset = radius - (int)Math.sqrt(Math.max(0, radius * radius - dy * dy));
            }
            c.fill(x1 + inset, y, x2 - inset, y + 1, color);
        }
    }

    private void setSlider(double mouseX) {
        int x = left + 158;
        int end = left + panelWidth - 24;
        double f = Math.max(0.0, Math.min(1.0, (mouseX - x) / Math.max(1.0, end - x)));
        if (activeSlider == 1) ModConfig.menuOpacity = 0.72 + f * 0.28;
        if (activeSlider == 2) ModConfig.backgroundDim = f * 0.75;
        ModConfig.save();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        if (mx >= left + 145 && mx <= left + panelWidth - 10 && my >= top + 65 && my <= top + panelHeight - 38) scrollPanel.mouseDown(mx, my);
        if (mx >= left && mx <= left + panelWidth && my >= top && my <= top + 48) {
            dragging = true; dragOffsetX = mx - left; dragOffsetY = my - top; return true;
        }
        if (mx >= left + 8 && mx <= left + 130) {
            for (int i = 0; i < TABS.length; i++) {
                int y = top + 63 + i * 36;
                if (my >= y && my <= y + 29) { selectedTab = i; return true; }
            }
        }
        int x = left + 158;
        int right = left + panelWidth - 22;
        int bx = right - 45;
        if (selectedTab == 0 || selectedTab == 1 || selectedTab == 2 || selectedTab == 4) {
            if (mx >= bx - 5 && mx <= bx + 48) {
                if (selectedTab == 0 && my >= top + 82 && my <= top + 112) { ModConfig.particles = !ModConfig.particles; ModConfig.save(); return true; }
                if (selectedTab == 1) {
                    if (my >= top + 82 && my <= top + 112) { ModConfig.particles = !ModConfig.particles; ModConfig.save(); return true; }
                    if (my >= top + 142 && my <= top + 170) { ModConfig.lowFire = !ModConfig.lowFire; ModConfig.save(); return true; }
                    if (my >= top + 182 && my <= top + 210) { ModConfig.smoothCamera = !ModConfig.smoothCamera; ModConfig.save(); return true; }
                    if (my >= top + 222 && my <= top + 250) { ModConfig.skyVisual = !ModConfig.skyVisual; ModConfig.save(); return true; }
                    if (my >= top + 262 && my <= top + 290) { ModConfig.rgbLighting = !ModConfig.rgbLighting; ModConfig.save(); return true; }
                }
                if (selectedTab == 2) {
                    if (my >= top + 82 && my <= top + 112) { ModConfig.cleanHud = !ModConfig.cleanHud; ModConfig.save(); return true; }
                    if (my >= top + 126 && my <= top + 156) { ModConfig.showCoordinates = !ModConfig.showCoordinates; ModConfig.save(); return true; }
                }
                if (selectedTab == 4 && my >= top + 82 && my <= top + 112) { ModConfig.menuAnimations = !ModConfig.menuAnimations; ModConfig.save(); return true; }
            }
        }
        if (selectedTab == 3 && mx >= x && mx <= right && my >= top + 66 && my <= top + panelHeight - 25) {
            int row = gameScroll + (int)((my - (top + 66)) / 22.0);
            if (row >= 0 && row < 20) { changeGameSetting(row); return true; }
        }
        if (selectedTab == 5 && my >= top + 110 && my <= top + 158) {
            if (mx >= x && mx <= x + 50) ModConfig.accentTheme = 0;
            else if (mx >= x + 58 && mx <= x + 110) ModConfig.accentTheme = 1;
            else if (mx >= x + 116 && mx <= x + 170) ModConfig.accentTheme = 2;
            else return super.mouseClicked(mx, my, button);
            ModConfig.save(); return true;
        }
        if (selectedTab == 6) {
            if (my >= top + 167 && my <= top + 191) activeSlider = 1;
            else if (my >= top + 236 && my <= top + 260) activeSlider = 2;
            if (activeSlider >= 0) { sliderDragging = true; setSlider(mx); return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        return scrollPanel.wheel(mx, my, verticalAmount) || super.mouseScrolled(mx, my, horizontalAmount, verticalAmount);
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && scrollPanel.mouseMove(mx, my)) { contentDragConsumed = true; return true; }
        if (dragging && button == 0) {
            left = Math.max(0, Math.min(width - panelWidth, (int)Math.round(mx - dragOffsetX)));
            top = Math.max(0, Math.min(height - panelHeight, (int)Math.round(my - dragOffsetY)));
            return true;
        }
        if (sliderDragging && button == 0) { setSlider(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragging = false; sliderDragging = false; activeSlider = -1;
            boolean consumed = contentDragConsumed; contentDragConsumed = false; scrollPanel.mouseUp();
            if (consumed) return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override public boolean shouldPause() { return false; }
}
