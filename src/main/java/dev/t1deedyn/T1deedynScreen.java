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
    private final SliderHitboxes berrySliderHitboxes = new SliderHitboxes();
    private int berryViewportX, berryViewportY, berryViewportW, berryViewportH;
    private final ModuleCard autoBerryCard = new ModuleCard();
    private boolean awaitingBerryKey;
    private static final int BERRY_BODY_HEIGHT = 620;
    private static final String[] BERRY_INT_TEXT = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};
    private static final String[] BERRY_RADIUS_TEXT = {"1.0","1.5","2.0","2.5","3.0","3.5","4.0","4.5","5.0"};
    private boolean pendingCardHeader;
    private boolean pendingCardToggle;
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
        if (awaitingBerryKey) { awaitingBerryKey=false; if(keyCode!=GLFW.GLFW_KEY_ESCAPE){ModConfig.autoBerryToggleKey=keyCode; T1deedynClient.setAutoBerryKey(keyCode); ModConfig.save();} return true; }
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
        berryViewportX = x - 8; berryViewportY = viewportY; berryViewportW = contentRight - x + 16;
        int viewportH = Math.max(32, panelHeight - 104);
        berryViewportH = viewportH;
        if(selectedTab==3) autoBerryCard.tick();
        int contentH = switch (selectedTab) { case 1 -> 230; case 2 -> 170; case 3 -> 110 + autoBerryCard.animatedContentHeight(BERRY_BODY_HEIGHT); case 4 -> 150; case 5 -> 220; case 6 -> 300; default -> 300; };
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
                toggle(c, x, py + 278, contentRight, "Раскрывать по одной", ModConfig.autoBerryAccordion);
                slider(c, x, py + 246, contentRight, ModConfig.backgroundDim, 0.0, 0.75, 2);
                c.drawText(textRenderer, "X или ESC  •  закрыть меню", x, py + panelHeight - 28, MUTED, false);
            }
            default -> {
                c.drawText(textRenderer, "Добро пожаловать!", x, py + 88, TEXT, false);
                c.drawText(textRenderer, "Меню открывается и закрывается клавишей X.", x, py + 112, MUTED, false);
                c.drawText(textRenderer, "Настрой внешний вид во вкладке «Настройки».", x, py + 136, MUTED, false);
                rounded(c, x, py + 166, contentRight, py + 222, 9, Theme.get(accent()).panelTint);
                c.drawBorder(x, py + 166, contentRight - x, 56, Theme.get(accent()).accentDim);
                c.drawText(textRenderer, "СОЗДАТЕЛИ", x + 12, py + 173, MUTED, false);
                rounded(c, x + 12, py + 190, x + 30, py + 208, 4, accent());
                c.drawText(textRenderer, "T1deedyn", x + 38, py + 193, Theme.get(accent()).accentBright, false);
                c.drawText(textRenderer, "&", x + 108, py + 193, 0x99AAA5B8, false);
                c.drawText(textRenderer, "PvE_BoTiK", x + 123, py + 193, Theme.get(accent()).accentBright, false);
                rounded(c, x, py + 234, x + 205, py + 280, 9, 0xFF282333);
                c.drawText(textRenderer, "T1  •  CUSTOM VISUALS", x + 14, py + 251, accent(), false);
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
        int y=py+70;
        autoBerryCard.renderHeader(c,x,y,right-x,accent(),ModConfig.autoBerryEnabled,
                mouseX>=x&&mouseX<=right&&mouseY>=y-scrollPanel.offset()&&mouseY<=y+40-scrollPanel.offset(),mouseX,mouseY);
        int h=autoBerryCard.animatedContentHeight(BERRY_BODY_HEIGHT);
        if(h<=0)return;
        c.enableScissor(x,y+40,right,y+40+h);
        int cy=y+48;
        cy=berrySection(c,x,cy,"ОСНОВНОЕ");
        cy=berrySlider(c,x,right,cy,0,"Радиус, блоков",ModConfig.autoBerryRadius,1,5);
        cy=berryToggle(c,x,right,cy,1,"Ограничить дальностью игры",ModConfig.autoBerryLimitReach);
        cy=berrySlider(c,x,right,cy,2,"Задержка между кликами, тики",ModConfig.autoBerryClickDelay,1,20);
        cy=berrySlider(c,x,right,cy,3,"Случайность задержки, тики",ModConfig.autoBerryRandomDelay,0,5);
        cy=berrySlider(c,x,right,cy,4,"Макс. кликов в секунду",ModConfig.autoBerryMaxCps,1,10);
        cy=berrySlider(c,x,right,cy,5,"Частота сканирования, тики",ModConfig.autoBerryScanTicks,1,20);
        cy=berrySection(c,x,cy,"ЧТО СОБИРАТЬ");
        cy=berryToggle(c,x,right,cy,6,"Сладкие ягоды",ModConfig.autoBerrySweet);
        cy=berryToggle(c,x,right,cy,7,"Светящиеся ягоды",ModConfig.autoBerryGlow);
        cy=berrySegment(c,x,right,cy);
        cy=berrySection(c,x,cy,"БЕЗОПАСНОСТЬ");
        cy=berryToggle(c,x,right,cy,9,"Безопасный режим руки",ModConfig.autoBerrySafeHand);
        cy=berryToggle(c,x,right,cy,10,"Только если стою на месте",ModConfig.autoBerryOnlyStationary);
        cy=berryToggle(c,x,right,cy,11,"Не работать при открытом экране",ModConfig.autoBerryPauseWithScreen);
        cy=berryToggle(c,x,right,cy,12,"Не работать при использовании предмета",ModConfig.autoBerryPauseUsingItem);
        cy=berryToggle(c,x,right,cy,13,"Видимость цели, raycast",ModConfig.autoBerryVisibleOnly);
        cy=berrySection(c,x,cy,"ЭФФЕКТЫ И ИНТЕРФЕЙС");
        cy=berryToggle(c,x,right,cy,14,"Взмах руки",ModConfig.autoBerrySwing);
        cy=berryToggle(c,x,right,cy,15,"Частицы на собранном кусте",ModConfig.autoBerryParticles);
        cy=berryToggle(c,x,right,cy,16,"Подсвечивать спелые кусты",ModConfig.autoBerryHighlight);
        cy=berryToggle(c,x,right,cy,17,"Счётчик «Собрано: N»",ModConfig.autoBerryCountHud);
        rounded(c,x,cy+1,right,cy+24,6,0xFF302B39);
        c.drawText(textRenderer,awaitingBerryKey?"Нажми клавишу (ESC отмена)":(ModConfig.autoBerryToggleKey<0?"Клавиша вкл/выкл: не назначена":"Клавиша вкл/выкл: назначена"),x+8,cy+7,TEXT,false);
        cy+=28;
        rounded(c,x,cy+1,right,cy+25,6,0xFF49303B);
        c.drawText(textRenderer,"Сбросить настройки модуля",x+9,cy+8,0xFFFFDCE3,false);
        c.disableScissor();
    }

    private int berrySection(DrawContext c,int x,int y,String name){c.drawText(textRenderer,name,x+3,y+2,0xFF9690A2,false);return y+20;}
    private int berryToggle(DrawContext c,int x,int right,int y,int id,String name,boolean on){
        c.drawText(textRenderer,name,x+5,y+5,ModConfig.autoBerryEnabled?TEXT:0xB3F5F2FF,false);
        int tx=right-40;c.fill(tx,y+4,tx+32,y+20,on?accent():0xFF484451);
        int k=on?tx+18:tx+2;c.fill(k,y+6,k+12,y+18,0xFFF7F4FF);return y+24;
    }
    private int berrySlider(DrawContext c,int x,int right,int y,int id,String name,double value,double min,double max){
        c.drawText(textRenderer,name,x+5,y+1,TEXT,false);
        int sx=x+8,ex=right-48,ty=y+16;
        SliderHitboxes.Rect rect=berrySliderHitboxes.get(id);
        rect.set(sx-berryViewportX,ty-9-berryViewportY+scrollPanel.offset(),ex-sx,22);
        c.fill(sx,ty,ex,ty+3,0xFF403B4A);
        int k=sx+(int)Math.round((ex-sx)*(value-min)/(max-min));c.fill(sx,ty,k,ty+3,accent());c.fill(k-4,ty-3,k+5,ty+8,0xFFF5F0FF);
        String v=id==0?BERRY_RADIUS_TEXT[(int)Math.max(0,Math.min(8,Math.round((value-1.0)*2.0)))]:BERRY_INT_TEXT[(int)Math.max(0,Math.min(20,value))];
        rounded(c,right-42,y,right-3,y+17,5,0xFF302B39);c.drawText(textRenderer,v,right-37,y+5,TEXT,false);return y+26;
    }
    private void setBerrySlider(int id,double mx){
        SliderHitboxes.Rect rect=berrySliderHitboxes.get(id);
        int sx=berryViewportX+rect.x, ex=sx+rect.w;
        double f=Math.max(0,Math.min(1,(mx-sx)/(double)Math.max(1,ex-sx)));
        switch(id){case 0->ModConfig.autoBerryRadius=1.0+Math.round(f*8.0)/2.0;case 2->ModConfig.autoBerryClickDelay=1+(int)Math.round(f*19);case 3->ModConfig.autoBerryRandomDelay=(int)Math.round(f*5);case 4->ModConfig.autoBerryMaxCps=1+(int)Math.round(f*9);case 5->ModConfig.autoBerryScanTicks=1+(int)Math.round(f*19);}
        ModConfig.save();
    }
    private int berrySegment(DrawContext c,int x,int right,int y){
        c.drawText(textRenderer,"Минимальная стадия",x+5,y+5,TEXT,false);
        int sx=right-172;rounded(c,sx,y,right,y+23,6,0xFF302B39);
        rounded(c,ModConfig.autoBerryStageTwo?sx+84:sx+2,y+2,ModConfig.autoBerryStageTwo?right-2:sx+82,y+21,5,accent());
        c.drawText(textRenderer,"Только спелые",sx+6,y+8,ModConfig.autoBerryStageTwo?MUTED:0xFFFFFFFF,false);
        c.drawText(textRenderer,"С 2-й стадии",sx+91,y+8,ModConfig.autoBerryStageTwo?0xFFFFFFFF:MUTED,false);
        return y+24;
    }
    private void toggleBerryOption(int id){
        switch(id){case 1->ModConfig.autoBerryLimitReach=!ModConfig.autoBerryLimitReach;case 6->ModConfig.autoBerrySweet=!ModConfig.autoBerrySweet;case 7->ModConfig.autoBerryGlow=!ModConfig.autoBerryGlow;case 8->ModConfig.autoBerryStageTwo=!ModConfig.autoBerryStageTwo;case 9->ModConfig.autoBerrySafeHand=!ModConfig.autoBerrySafeHand;case 10->ModConfig.autoBerryOnlyStationary=!ModConfig.autoBerryOnlyStationary;case 11->ModConfig.autoBerryPauseWithScreen=!ModConfig.autoBerryPauseWithScreen;case 12->ModConfig.autoBerryPauseUsingItem=!ModConfig.autoBerryPauseUsingItem;case 13->ModConfig.autoBerryVisibleOnly=!ModConfig.autoBerryVisibleOnly;case 14->ModConfig.autoBerrySwing=!ModConfig.autoBerrySwing;case 15->ModConfig.autoBerryParticles=!ModConfig.autoBerryParticles;case 16->ModConfig.autoBerryHighlight=!ModConfig.autoBerryHighlight;case 17->ModConfig.autoBerryCountHud=!ModConfig.autoBerryCountHud;}
        ModConfig.save();
    }
    private void resetBerrySettings(){
        ModConfig.autoBerryEnabled=false;ModConfig.autoBerryRadius=3.0;ModConfig.autoBerryLimitReach=true;ModConfig.autoBerryClickDelay=3;ModConfig.autoBerryRandomDelay=1;ModConfig.autoBerryMaxCps=5;ModConfig.autoBerryScanTicks=5;
        ModConfig.autoBerrySweet=true;ModConfig.autoBerryGlow=true;ModConfig.autoBerryStageTwo=false;ModConfig.autoBerrySafeHand=true;ModConfig.autoBerryOnlyStationary=false;ModConfig.autoBerryPauseWithScreen=true;ModConfig.autoBerryPauseUsingItem=true;ModConfig.autoBerryVisibleOnly=false;
        ModConfig.autoBerrySwing=true;ModConfig.autoBerryParticles=false;ModConfig.autoBerryHighlight=false;ModConfig.autoBerryCountHud=true;ModConfig.autoBerryToggleKey=-1;
        T1deedynClient.setAutoBerryKey(-1);ModConfig.save();
    }

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
        if(button==1&&selectedTab==3){int cx=left+158,cr=left+panelWidth-22,cy=top+70-scrollPanel.offset();if(mx>=cx&&mx<=cr&&my>=cy&&my<=cy+40){pendingCardHeader=true;pendingCardToggle=true;return true;}}
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
        if(selectedTab==3){
            int cy=top+70-scrollPanel.offset();
            if(mx>=x&&mx<=right&&my>=cy&&my<=cy+40){pendingCardHeader=true;pendingCardToggle=mx>=right-70;return true;}
            if(autoBerryCard.isExpanded() && mx>=berryViewportX && mx<berryViewportX+berryViewportW
                    && my>=berryViewportY && my<berryViewportY+berryViewportH
                    && mx>=x && mx<=right && my>cy+40 && my<cy+40+BERRY_BODY_HEIGHT){
                int contentX=(int)mx-berryViewportX;
                int contentY=(int)my-berryViewportY+scrollPanel.offset();
                int sliderId=berrySliderHitboxes.find(contentX,contentY);
                if(sliderId>=0){
                    activeSlider=10+sliderId;
                    sliderDragging=true;
                    setBerrySlider(sliderId,mx);
                    return true;
                }
                int ry=(int)my+scrollPanel.offset()-(top+70+48);
                int[] rowStart={20,46,70,96,122,148,194,218,242,286,310,334,358,382,426,450,474,498,522,550};
                int[] rowEnd={46,70,96,122,148,174,218,242,266,310,334,358,382,406,450,474,498,522,550,580};
                int hit=-1;for(int i=0;i<rowStart.length;i++)if(ry>=rowStart[i]&&ry<rowEnd[i]){hit=i;break;}
                if(hit==1||hit==6||hit==7||hit==8||hit==9||hit==10||hit==11||hit==12||hit==13||hit==14||hit==15||hit==16||hit==17){toggleBerryOption(hit);return true;}
                if(hit==18){awaitingBerryKey=true;return true;}
                if(hit==19){resetBerrySettings();return true;}
            }
        }
        if (selectedTab == 5 && my >= top + 110 && my <= top + 158) {
            if (mx >= x && mx <= x + 50) ModConfig.accentTheme = 0;
            else if (mx >= x + 58 && mx <= x + 110) ModConfig.accentTheme = 1;
            else if (mx >= x + 116 && mx <= x + 170) ModConfig.accentTheme = 2;
            else return super.mouseClicked(mx, my, button);
            ModConfig.save(); return true;
        }
        if (selectedTab == 6) {
            if(my>=top+270&&my<=top+302&&mx>=right-55){ModConfig.autoBerryAccordion=!ModConfig.autoBerryAccordion;ModConfig.save();return true;}
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
        if (sliderDragging && button == 0) { if(activeSlider>=10)setBerrySlider(activeSlider-10,mx);else setSlider(mx); return true; }
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
        if(button==1 && pendingCardHeader){if(pendingCardToggle)AutoBerry.toggle();else autoBerryCard.toggle();pendingCardHeader=false;pendingCardToggle=false;return true;}
        if (button == 0) {
            dragging = false; sliderDragging = false; activeSlider = -1;
            boolean consumed = contentDragConsumed || scrollPanel.consumedClick(); contentDragConsumed = false; scrollPanel.mouseUp();
            if(pendingCardHeader){if(!consumed){if(pendingCardToggle)AutoBerry.toggle();else autoBerryCard.toggle();}pendingCardHeader=false;pendingCardToggle=false;return true;}
            if (consumed) return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override public boolean shouldPause() { return false; }
}
