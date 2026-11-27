package dev.t1deedyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Responsive integer-coordinate interface for Minecraft 1.21 / Yarn. */
public final class BlackCloverScreen extends Screen {
    private static final String[] TABS={"Визуал","Игрок","Мир","Интерфейс","HUD","Эффекты","Настройки"};
    private static final String[] DESCRIPTIONS={
        "Настройка визуальных эффектов и отображения","Настройки игрока и автоматизации",
        "Окружение, погода и подсветка мира","Масштаб, прозрачность и оформление",
        "Информация и элементы игрового HUD","Частицы и визуальные эффекты",
        "Профили, производительность и информация"
    };
    private static final int ROW_HEIGHT=56, ROW_GAP=6, ROW_STEP=ROW_HEIGHT+ROW_GAP, GAP=8;
    private final List<CloverFeature> visibleFeatures=new ArrayList<>();
    private final Map<String,String> ellipsisCache=new HashMap<>();
    private UiLayout layout;
    private int selectedTab, selectedFeature=-1, scrollOffset, sidebarScroll, settingsScroll;
    private final int[] tabScrollOffsets=new int[TABS.length];
    private int listContentHeight;
    private double pressStartX,pressStartY;
    private boolean touchScrolling,draggingListScrollbar,pendingRowToggle;
    private int pendingRow=-1;
    private int listDragStartY,listDragStartOffset;
    private boolean searchFocused, profileModal, themeDropdown, profileDropdown;
    private String search="", profileName="profile-1", selectedProfile="";
    private int draggingSlider=-1, animationTicks, hoverRow=-1;
    private int lastWidth=-1,lastHeight=-1;
    private int rightContentHeight;
    private int profileMenuX, themeMenuX;
    private final double previousScale;
    private boolean scaleRestored;

    public BlackCloverScreen(){
        super(Text.literal("Чёрный Клевер"));
        MinecraftClient client=MinecraftClient.getInstance();
        previousScale=client.getWindow().getScaleFactor();
        applyMenuScale(client);
    }
    private void applyMenuScale(MinecraftClient client){
        var window=client.getWindow();
        int selected=ModConfig.uiScale;
        int scale;
        if(selected==0){
            scale=1;
            int fw=window.getWidth(),fh=window.getHeight();
            while(scale<4&&fw/(scale+1)>=760&&fh/(scale+1)>=420)scale++;
        }else scale=clamp(selected,1,4);
        window.setScaleFactor(scale);
        client.onResolutionChanged();
    }
    @Override public void removed(){
        if(!scaleRestored){
            scaleRestored=true;
            MinecraftClient client=MinecraftClient.getInstance();
            client.getWindow().setScaleFactor(previousScale);
            client.onResolutionChanged();
        }
        super.removed();
    }
    @Override protected void init(){
        layout=new UiLayout(width,height);
        lastWidth=width;lastHeight=height;
        refreshFeatures();
        calculateRightContent();
    }
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext c,int mouseX,int mouseY,float delta){
        int alpha=(int)Math.round(clamp(ModConfig.backgroundDim,0.0,0.75)*255.0);
        c.fill(0,0,width,height,alpha<<24);
    }
    private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
    private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
    private void refreshFeatures(){
        visibleFeatures.clear();
        String q=search.trim().toLowerCase(java.util.Locale.ROOT);
        for(CloverFeature feature:CloverFeature.ALL){
            if(q.isEmpty()&&feature.tab!=selectedTab)continue;
            if(!q.isEmpty()&&!feature.name.toLowerCase(java.util.Locale.ROOT).contains(q)
                    &&!feature.description.toLowerCase(java.util.Locale.ROOT).contains(q))continue;
            visibleFeatures.add(feature);
        }
        listContentHeight=20+visibleFeatures.size()*ROW_HEIGHT+Math.max(0,visibleFeatures.size()-1)*ROW_GAP;
        int max=Math.max(0,listContentHeight-layout.centerViewportHeight);
        scrollOffset=clamp(scrollOffset,0,max);
        if(selectedFeature>=visibleFeatures.size())selectedFeature=-1;
    }
    private void calculateRightContent(){rightContentHeight=selectedFeature>=0?196:390;}
    private int shortText(String value,int maxWidth){
        return textRenderer.getWidth(value)<=maxWidth?textRenderer.getWidth(value):maxWidth;
    }
    private String ellipsize(String value,int maxWidth){
        if(maxWidth<=0)return "";
        String key=maxWidth+"|"+value;
        String cached=ellipsisCache.get(key);
        if(cached!=null)return cached;
        if(textRenderer.getWidth(value)<=maxWidth){ellipsisCache.put(key,value);return value;}
        String suffix="…";
        int suffixWidth=textRenderer.getWidth(suffix);
        String cut=textRenderer.trimToWidth(value,Math.max(0,maxWidth-suffixWidth));
        String result=cut+suffix;
        ellipsisCache.put(key,result);
        return result;
    }
    private void drawText(DrawContext c,String value,int x,int y,int color,int maxWidth){
        c.enableScissor(x,y-1,x+Math.max(0,maxWidth),y+11);
        try{c.drawText(textRenderer,ellipsize(value,maxWidth),x,y,color,false);}
        finally{c.disableScissor();}
    }
    private void drawIcon(DrawContext c,int index,int x,int y,int color){
        switch(index){
            case 0 -> { // eye
                c.drawHorizontalLine(x+1,x+16,y+8,color); c.drawVerticalLine(x+1,y+5,y+11,color);
                c.drawVerticalLine(x+16,y+5,y+11,color); c.fill(x+5,y+5,x+13,y+12,color);
                c.fill(x+8,y+6,x+11,y+11,Theme.PANEL);
            }
            case 1 -> {c.fill(x+6,y+1,x+12,y+8,color);c.fill(x+3,y+9,x+15,y+17,color);c.fill(x+5,y+12,x+13,y+17,Theme.PANEL);}
            case 2 -> {c.fill(x+2,y+1,x+17,y+16,color);c.fill(x+4,y+3,x+15,y+14,Theme.PANEL);c.drawHorizontalLine(x+3,x+16,y+8,color);c.drawVerticalLine(x+9,y+2,y+15,color);c.drawVerticalLine(x+6,y+3,y+14,color);c.drawVerticalLine(x+12,y+3,y+14,color);}
            case 3 -> {RoundedRect.border(c,x+1,y+1,17,12,2,1,color,Theme.PANEL);c.fill(x+8,y+13,x+11,y+16,color);c.fill(x+5,y+16,x+14,y+17,color);}
            case 4 -> {c.fill(x+2,y+10,x+5,y+17,color);c.fill(x+7,y+5,x+10,y+17,color);c.fill(x+12,y+1,x+15,y+17,color);}
            case 5 -> {c.fill(x+8,y,x+10,y+18,color);c.fill(x,y+8,x+18,y+10,color);c.fill(x+3,y+3,x+6,y+6,color);c.fill(x+12,y+12,x+15,y+15,color);}
            case 6 -> {for(int i=0;i<8;i++){double a=i*Math.PI/4;int tx=x+9+(int)Math.round(Math.cos(a)*7),ty=y+9+(int)Math.round(Math.sin(a)*7);c.fill(tx-1,ty-1,tx+2,ty+2,color);}c.fill(x+4,y+4,x+14,y+14,color);c.fill(x+7,y+7,x+11,y+11,Theme.PANEL);}
        }
    }
    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){
        if(layout==null||width!=lastWidth||height!=lastHeight){layout=new UiLayout(width,height);lastWidth=width;lastHeight=height;refreshFeatures();calculateRightContent();}
        mouseXCache=mouseX;mouseYCache=mouseY;
        renderBackground(c,mouseX,mouseY,delta);
        renderAtmosphere(c);
        int x=layout.x,y=layout.y,w=layout.width,h=layout.height;
        int accent=Theme.accentColor(),bright=Theme.brightAccentColor();
        c.enableScissor(0,0,width,height);
        try{
        c.enableScissor(x,y,x+w,y+h);
        try{
            RoundedRect.fill(c,x-2,y-2,w+4,h+4,16,0x182BE36B);
            RoundedRect.border(c,x,y,w,h,UiLayout.WINDOW_RADIUS,1,bright,Theme.BG);
            renderSidebar(c,mouseX,mouseY);
            renderTopbar(c,mouseX,mouseY);
            renderFeatureList(c,mouseX,mouseY);
            if(!layout.compact)renderSettings(c,mouseX,mouseY);
            if(profileModal)renderProfileModal(c);
            if(themeDropdown)renderThemeDropdown(c);
            if(profileDropdown)renderProfileDropdown(c);
        } finally {c.disableScissor();}
        renderCreators(c);
        } finally {c.disableScissor();}
        if(ModConfig.showBounds)drawBounds(c);
    }
    private void renderSidebar(DrawContext c,int mx,int my){
        int sx=layout.sidebarX,sy=layout.y+GAP,sw=layout.sidebarWidth;
        RoundedRect.border(c,sx,sy,sw,layout.height-GAP*2,10,1,Theme.BORDER,surface(Theme.panelColor()));
        int logo=layout.logoHeight;
        int mark=Math.min(56,Math.max(40,logo-42));
        CloverMark.draw(c,sx+(sw-mark)/2,sy+5,mark,Theme.accentColor());
        int logoTextY=sy+5+mark+6;
        drawCentered(c,"ЧЁРНЫЙ",sx+sw/2,logoTextY,Theme.TEXT);
        drawCentered(c,"КЛЕВЕР",sx+sw/2,logoTextY+11,Theme.TEXT);
        int vx=layout.sidebarViewportX,vy=layout.sidebarViewportY,vw=layout.sidebarViewportWidth,vh=layout.sidebarViewportHeight;
        c.enableScissor(vx,vy,vx+vw,vy+vh);
        try{
            int rowH=layout.tabHeight;
            for(int i=0;i<TABS.length;i++){
                int ty=vy+i*(rowH+4)-sidebarScroll;
                if(ty+rowH<vy||ty>vy+vh)continue;
                boolean active=i==selectedTab,hover=inside(mx,my,vx,ty,vw,rowH);
                if(active){int a=Theme.accentColor(),b=Theme.accent2Color();RoundedRect.fill(c,vx,ty,vw,rowH,7,(a&0x00FFFFFF)|0x8C000000);c.fill(vx+1,ty+rowH/2,vx+vw-1,ty+rowH-1,(b&0x00FFFFFF)|0x40000000);RoundedRect.border(c,vx,ty,vw,rowH,7,1,Theme.brightAccentColor(),0x00000000);}
                else if(hover)RoundedRect.fill(c,vx,ty,vw,rowH,7,surface(Theme.CARD_HOVER));
                int iconY=ty+(rowH-18)/2;
                drawIcon(c,i,vx+5,iconY,active?Theme.TEXT:Theme.TEXT_DIM);
                if(vw>88)drawText(c,TABS[i],vx+29,ty+(rowH-9)/2,active?Theme.TEXT:Theme.TEXT_DIM,vw-34);
            }
        }finally{c.disableScissor();}
        drawScrollbar(c,vx+vw-3,vy,vh,TABS.length*(layout.tabHeight+4),sidebarScroll,Theme.accentColor());
        int fy=layout.footerY;
        c.fill(sx+8,fy-4,sx+sw-8,fy-3,Theme.BORDER);
        String footer="✦ Minecraft 1.21.0 • Fabric";
        int max=sw-16;
        if(textRenderer.getWidth(footer)>max)footer="1.21.0 • Fabric";
        if(textRenderer.getWidth(footer)>max)footer=textRenderer.trimToWidth(footer,max);
        c.drawText(textRenderer,footer,sx+8,fy+2,Theme.TEXT_DIM,false);
        if(ModConfig.showBounds){RoundedRect.border(c,vx,vy,vw,vh,2,1,0xFFFFAA33,0x00000000);c.drawText(textRenderer,"content="+listContentHeight+" viewport="+vh+" max="+Math.max(0,listContentHeight-vh)+" offset="+scrollOffset,vx,vy+2,0xFFFFAA33,false);}
    }
    private void renderTopbar(DrawContext c,int mx,int my){
        int left=layout.centerX,right=layout.compact?layout.x+layout.width-GAP:layout.settingsX+layout.settingsWidth;
        int top=layout.y+GAP,barW=right-left;
        RoundedRect.border(c,left,top,barW,UiLayout.TOPBAR_HEIGHT,8,1,Theme.BORDER,Theme.PANEL);
        int closeW=26,minW=26;
        RoundedRect.fill(c,right-closeW-4,top+5,closeW,26,6,Theme.CARD);
        RoundedRect.fill(c,right-closeW-minW-8,top+5,minW,26,6,Theme.CARD);
        drawCentered(c,"×",right-closeW/2-4,top+13,Theme.TEXT);
        drawCentered(c,"—",right-closeW-minW/2-8,top+13,Theme.TEXT);
        int available=Math.max(90,barW-2*(closeW+minW)-32);
        int searchW=Math.max(90,Math.min(available/2,barW/3));
        searchW=Math.min(searchW,Math.max(90,barW-160));
        RoundedRect.fill(c,left+6,top+5,searchW,26,6,Theme.CARD);
        c.drawText(textRenderer,"⌕",left+13,top+13,Theme.TEXT_DIM,false);
        drawText(c,search.isEmpty()?"Поиск функций…":search,left+29,top+13,searchFocused?Theme.TEXT:Theme.TEXT_DIM,searchW-35);
        int controlsX=left+searchW+12;
        int controlRight=right-closeW-minW-16;
        int remaining=controlRight-controlsX;
        if(remaining>=58){
            int pw=Math.min(118,remaining/2-3);
            profileMenuX=controlsX;
            themeMenuX=controlsX+pw+5;
            RoundedRect.border(c,controlsX,top+5,pw,26,6,1,Theme.BORDER,Theme.CARD);
            CloverMark.draw(c,controlsX+5,top+11,13,Theme.accentColor());
            if(pw>=88)drawText(c,"Профиль 1",controlsX+23,top+13,Theme.TEXT,pw-39);
            else drawCentered(c,"♣",controlsX+pw/2,top+13,Theme.TEXT);
            c.drawText(textRenderer,"⌄",controlsX+pw-13,top+13,Theme.TEXT_DIM,false);
            int tx=controlsX+pw+5,tw=controlRight-tx;
            if(tw>=30){
                RoundedRect.border(c,tx,top+5,tw,26,6,1,Theme.BORDER,Theme.CARD);
                if(tw>=64)drawText(c,"☾ Тема",tx+6,top+13,Theme.accentColor(),tw-18);
                else drawCentered(c,"☾",tx+tw/2,top+13,Theme.accentColor());
                c.drawText(textRenderer,"⌄",tx+tw-12,top+13,Theme.TEXT_DIM,false);
            }
        }
    }
    private void renderFeatureList(DrawContext c,int mx,int my){
        int cx=layout.centerX,cy=layout.contentY,cw=layout.centerWidth,ch=layout.contentHeight;
        RoundedRect.border(c,cx,cy,cw,ch,9,1,Theme.BORDER,surface(Theme.PANEL));
        CloverMark.draw(c,cx+10,cy+10,26,Theme.accentColor());
        c.drawText(textRenderer,search.isEmpty()?TABS[selectedTab].toUpperCase(java.util.Locale.ROOT):"РЕЗУЛЬТАТЫ ПОИСКА",cx+43,cy+17,Theme.accentColor(),false);
        if(!layout.compact)drawText(c,DESCRIPTIONS[selectedTab],cx+12,cy+43,Theme.TEXT_DIM,cw-24);
        int vx=layout.centerViewportX,vy=layout.centerViewportY,vw=layout.centerViewportWidth,vh=layout.centerViewportHeight;
        hoverRow=-1;
        c.enableScissor(vx,vy,vx+vw,vy+vh);
        try{
            for(int i=0;i<visibleFeatures.size();i++){
                int ry=vy+10+i*ROW_STEP-scrollOffset;
                if(ry+ROW_HEIGHT<vy||ry>vy+vh)continue;
                CloverFeature f=visibleFeatures.get(i);
                boolean hovered=inside(mx,my,vx,ry,vw,ROW_HEIGHT-4);
                if(hovered)hoverRow=i;
                if(i==selectedFeature)RoundedRect.border(c,vx,ry,vw,ROW_HEIGHT-4,7,1,Theme.brightAccentColor(),Theme.CARD);
                else RoundedRect.fill(c,vx,ry,vw,ROW_HEIGHT-4,7,hovered?Theme.CARD_HOVER:Theme.CARD);
                c.fill(vx+7,ry+10,vx+9,ry+ROW_HEIGHT-14,i==selectedFeature?Theme.accentColor():Theme.BORDER);
                int iconX=vx+8, textX=iconX+36+10, toggleX=vx+vw-50;
                int textW=Math.max(1,vw-10-36-10-8-40-10-10);
                c.fill(iconX+8,ry+18,iconX+24,ry+34,i==selectedFeature?Theme.accentColor():Theme.BORDER);
                drawText(c,f.name,textX,ry+10,Theme.TEXT,textW);
                drawText(c,f.description,textX,ry+29,Theme.TEXT_DIM,textW);
                drawToggle(c,toggleX,ry+18,f.enabled(),Theme.accentColor());
            }
        }finally{c.disableScissor();}
        drawScrollbar(c,vx+vw-3,vy,vh,listContentHeight,scrollOffset,Theme.accentColor());
        if(scrollOffset>0)c.fill(vx,vy,vx+vw,vy+2,0x182BE36B);
        if(scrollOffset<Math.max(0,listContentHeight-vh))c.fill(vx,vy+vh-2,vx+vw,vy+vh,0x182BE36B);
        if(hoverRow>=0&&!layout.compact){
            CloverFeature f=visibleFeatures.get(hoverRow);
            if(textRenderer.getWidth(f.description)>Math.max(12,vw-90)){
                int tw=Math.min(260,textRenderer.getWidth(f.description)+12),tx=clamp(mx+10,layout.x,layout.x+layout.width-tw);
                RoundedRect.fill(c,tx,my+12,tw,22,5,0xF0101813);
                c.drawText(textRenderer,textRenderer.trimToWidth(f.description,tw-10),tx+5,my+19,Theme.TEXT,false);
            }
        }
    }
    private void renderSettings(DrawContext c,int mx,int my){
        int sx=layout.settingsX,sy=layout.contentY,sw=layout.settingsWidth,sh=layout.contentHeight;
        RoundedRect.border(c,sx,sy,sw,sh,9,1,Theme.BORDER,surface(Theme.PANEL));
        boolean general=selectedFeature<0||selectedFeature>=visibleFeatures.size();
        CloverMark.draw(c,sx+9,sy+9,24,Theme.accentColor());
        drawText(c,general?"ОБЩИЕ НАСТРОЙКИ МЕНЮ":"НАСТРОЙКИ ФУНКЦИИ",sx+39,sy+17,Theme.accentColor(),sw-48);
        int vx=layout.settingsViewportX,vy=layout.settingsViewportY,vw=layout.settingsViewportWidth,vh=layout.settingsViewportHeight;
        int total=general?390:196;
        c.enableScissor(vx,vy,vx+vw,vy+vh);
        try{
            int cy=vy-settingsScroll;
            if(general){
                c.drawText(textRenderer,"ПРЕДПРОСМОТР HUD",vx,cy+2,Theme.TEXT,false);
                RoundedRect.fill(c,vx,cy+22,vw,56,7,Theme.CARD);
                c.drawText(textRenderer,"FPS: 144",vx+8,cy+29,Theme.TEXT,false);
                c.drawText(textRenderer,"XYZ: 120 64 -38",vx+8,cy+43,Theme.TEXT_DIM,false);
                c.drawText(textRenderer,"ЧЁРНЫЙ КЛЕВЕР",vx+8,cy+57,Theme.accentColor(),false);
                drawSettingLabel(c,"Прозрачность меню",vx,cy+91,vw);
                drawSlider(c,vx+12,cy+109,vw-24,ModConfig.menuOpacity,0.72,1.0,0);
                drawSettingLabel(c,"Затемнение фона",vx,cy+145,vw);
                drawSlider(c,vx+12,cy+163,vw-24,ModConfig.backgroundDim,0.0,0.75,1);
                drawSettingLabel(c,"Цветовая тема",vx,cy+199,vw);
                RoundedRect.border(c,vx,cy+217,vw,28,6,1,Theme.borderColor(),Theme.cardColor());
                drawText(c,Theme.displayName(Theme.selected()),vx+8,cy+226,Theme.accentColor(),vw-16);
                RoundedRect.fill(c,vx,cy+253,vw,24,6,Theme.CARD_HOVER);
                drawCentered(c,ModConfig.tintMenuBackground?"Тонировать фон: ВКЛ":"Тонировать фон: ВЫКЛ",vx+vw/2,cy+261,Theme.TEXT);
                RoundedRect.fill(c,vx,cy+281,vw,22,6,Theme.CARD_HOVER);
                drawCentered(c,"Сбросить тему",vx+vw/2,cy+287,Theme.TEXT);
                drawSettingLabel(c,"Масштаб меню",vx,cy+310,vw);
                RoundedRect.border(c,vx,cy+328,vw,25,6,1,Theme.BORDER,Theme.CARD);
                String scale=ModConfig.uiScale==0?"Авто":Integer.toString(ModConfig.uiScale);
                drawCentered(c,scale+"  ⌄",vx+vw/2,cy+336,Theme.TEXT);
                RoundedRect.fill(c,vx,cy+357,vw,24,6,Theme.CARD_HOVER);
                drawCentered(c,ModConfig.showBounds?"Границы: ВКЛ":"Показывать границы",vx+vw/2,cy+365,Theme.TEXT);
            }else{
                CloverFeature feature=visibleFeatures.get(selectedFeature);
                c.drawText(textRenderer,feature.name,vx,cy+2,Theme.TEXT,false);
                drawText(c,feature.description,vx,cy+22,Theme.TEXT_DIM,vw);
                drawSettingLabel(c,"Состояние",vx,cy+58,vw);
                drawToggle(c,vx+vw-42,cy+76,feature.enabled(),Theme.accentColor());
                c.drawText(textRenderer,feature.enabled()?"Включено":"Выключено",vx,cy+105,feature.enabled()?Theme.accentColor():Theme.TEXT_DIM,false);
                drawSettingLabel(c,"Дополнительные параметры",vx,cy+143,vw);
            }
        }finally{c.disableScissor();}
        drawScrollbar(c,vx+vw-2,vy,vh,total,settingsScroll,Theme.accentColor());
        if(ModConfig.showBounds)RoundedRect.border(c,vx,vy,vw,vh,2,1,0xFF55AAFF,0x00000000);
    }
    private void drawSettingLabel(DrawContext c,String label,int x,int y,int w){drawText(c,label,x,y,Theme.TEXT_DIM,w);}
    private void drawSlider(DrawContext c,int x,int y,int w,double value,double min,double max,int id){
        int trackW=Math.max(12,w),centerMin=x+6,centerMax=x+trackW-6,center= centerMin+(int)Math.round((centerMax-centerMin)*clamp((value-min)/(max-min),0,1));
        int trackY=y+13;
        RoundedRect.fill(c,x,trackY-2,trackW,4,2,Theme.TOGGLE_OFF);
        RoundedRect.fill(c,x,trackY-2,Math.max(0,center-x),4,2,Theme.accentColor());
        int knobSize=inside(mouseXCache,mouseYCache,center-7,trackY-7,14,14)?14:12;
        RoundedRect.fill(c,center-knobSize/2,trackY-knobSize/2,knobSize,knobSize,knobSize/2,Theme.accentColor());
        RoundedRect.fill(c,center-3,trackY-3,6,6,3,Theme.TEXT);
        int valueText=(int)Math.round(value*100);
        String val=id==0?valueText+"%":valueText+"%";
        RoundedRect.fill(c,x+trackW-38,y-1,38,14,5,Theme.CARD_HOVER);
        drawCentered(c,val,x+trackW-19,y+2,Theme.TEXT);
    }
    private double mouseXCache,mouseYCache;
    private void drawToggle(DrawContext c,int x,int y,boolean enabled,int accent){
        RoundedRect.fill(c,x,y,40,20,10,enabled?accent:Theme.TOGGLE_OFF);
        RoundedRect.fill(c,enabled?x+22:x+2,y+2,16,16,8,0xFFFFFFFF);
    }
    private void drawScrollbar(DrawContext c,int x,int y,int h,int content,int offset,int color){
        if(content<=h||h<=0)return;
        int thumb=Math.max(18,h*h/content);
        int max=content-h,top=y+(h-thumb)*offset/max;
        boolean hover=inside(mouseXCache,mouseYCache,x-3,y,8,h);
        int track=hover?6:3,tx=x-(track-3)/2;
        c.fill(tx,y,tx+track,y+h,hover?0x70465A4C:0x403A5143);
        RoundedRect.fill(c,tx,top,track,thumb,track/2,hover?color:(color&0x00FFFFFF)|0xA6000000);
    }
    private void renderCreators(DrawContext c){
        int tagW=Math.min(190,layout.width-20),tagX=layout.x+layout.width-tagW,tagY=layout.creatorY;
        RoundedRect.border(c,tagX,tagY,tagW,22,8,1,Theme.brightAccentColor(),Theme.PANEL);
        CloverMark.draw(c,tagX+6,tagY+4,14,Theme.accentColor());
        drawText(c,"T1deedyn & PvE_BoTiK",tagX+25,tagY+7,Theme.brightAccentColor(),tagW-31);
    }
    private void renderAtmosphere(DrawContext c){
        if(!ModConfig.menuAnimations)return;
        animationTicks++;
        c.fill(0,0,width,2,0x202BE36B);c.fill(0,height-2,width,height,0x182BE36B);
        int count=Math.min(20,Math.max(0,ModConfig.particleLimit));
        for(int i=0;i<count;i++){
            int px=(int)((i*97L+animationTicks*3L)%Math.max(1,width));
            int py=(int)((i*53L-animationTicks/2L+height*2L)%Math.max(1,height));
            CloverMark.draw(c,px,py,8,0x502BE36B);
        }
    }
    private void renderProfileModal(DrawContext c){
        int mw=Math.min(360,width-24),mh=226,mx=(width-mw)/2,my=(height-mh)/2;
        c.fill(0,0,width,height,0x99000000);RoundedRect.border(c,mx,my,mw,mh,12,1,Theme.brightAccentColor(),Theme.PANEL);
        drawCentered(c,"ПРОФИЛИ",width/2,my+12,Theme.brightAccentColor());
        RoundedRect.fill(c,mx+12,my+34,mw-24,24,6,Theme.CARD);
        drawText(c,profileName.isEmpty()?"Имя профиля…":profileName,mx+20,my+42,Theme.TEXT,mw-40);
        List<String> profiles=CloverProfiles.list();int rowY=my+66;
        for(int i=0;i<Math.min(4,profiles.size());i++){String n=profiles.get(i);if(n.equals(selectedProfile))RoundedRect.fill(c,mx+12,rowY+i*21,mw-24,19,5,Theme.CARD_HOVER);c.drawText(textRenderer,n,mx+20,rowY+5+i*21,Theme.TEXT,false);}
        int by=my+mh-34,buttonW=(mw-32)/4;
        String[] labels={"Сохранить","Переимен.","Загрузить","Удалить"};
        for(int i=0;i<4;i++){int bx=mx+12+i*(buttonW+2);RoundedRect.fill(c,bx,by,buttonW,23,6,Theme.CARD_HOVER);drawCentered(c,labels[i],bx+buttonW/2,by+7,i==3?Theme.DANGER:Theme.TEXT);}
    }
    private void renderThemeDropdown(DrawContext c){
        int w=190,h=190,x=clamp(themeMenuX,4,Math.max(4,width-w-4)),y=layout.y+48;
        RoundedRect.border(c,x,y,w,h,8,1,Theme.brightAccentColor(),Theme.panelColor());
        Theme.Preset[] p=Theme.Preset.values();
        for(int i=0;i<p.length;i++){
            int col=i/10,row=i%10,ix=x+6+col*92,iy=y+5+row*18;
            int color=i==Theme.selected().ordinal()?Theme.accentColor():Theme.accent2Color();
            RoundedRect.fill(c,ix,iy+3,9,9,5,color);
            drawText(c,Theme.displayName(p[i]),ix+14,iy+3,p[i]==Theme.selected()?Theme.TEXT:Theme.TEXT_DIM,72);
        }
    }
    private void renderProfileDropdown(DrawContext c){
        int x=profileMenuX,y=layout.y+48;
        RoundedRect.border(c,x,y,130,86,7,1,Theme.BORDER,Theme.PANEL);
        c.drawText(textRenderer,"Профиль 1",x+8,y+8,Theme.TEXT,false);
        c.drawText(textRenderer,"Новый профиль",x+8,y+26,Theme.TEXT_DIM,false);
        c.drawText(textRenderer,"Переименовать",x+8,y+44,Theme.TEXT_DIM,false);
        c.drawText(textRenderer,"Удалить",x+8,y+62,Theme.DANGER,false);
    }
    private int surface(int color){int a=(color>>>24)&255;int o=(int)Math.round(clamp(ModConfig.menuOpacity,0.72,1.0)*a);return(color&0x00FFFFFF)|(o<<24);}
    private void drawCentered(DrawContext c,String s,int x,int y,int color){c.drawText(textRenderer,s,x-textRenderer.getWidth(s)/2,y,color,false);}
    private static boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private void cycleTheme(){Theme.Preset[] p=Theme.Preset.values();Theme.select(p[(Theme.selected().ordinal()+1)%p.length]);ModConfig.accentTheme=Theme.selected().ordinal();ModConfig.accentHex=String.format(java.util.Locale.ROOT,"#%06X",Theme.accentColor()&0xFFFFFF);ModConfig.save();}
    private void setSliderValue(int id,double mouseX){
        int x=layout.settingsViewportX+12,w=layout.settingsViewportWidth-24;
        double ratio=clamp((mouseX-(x+6.0))/Math.max(1,w-12),0,1);
        if(id==0)ModConfig.menuOpacity=0.72+ratio*0.28;else ModConfig.backgroundDim=ratio*0.75;
        ModConfig.save();
    }
    private void drawBounds(DrawContext c){
        RoundedRect.border(c,layout.sidebarX,layout.y+GAP,layout.sidebarWidth,layout.height-GAP*2,2,1,0xFFFFAA33,0);
        RoundedRect.border(c,layout.centerX,layout.contentY,layout.compact?layout.width-(layout.centerX-layout.x)-GAP:layout.centerWidth,layout.contentHeight,2,1,0xFF55AAFF,0);
        if(!layout.compact)RoundedRect.border(c,layout.settingsX,layout.contentY,layout.settingsWidth,layout.contentHeight,2,1,0xFFFF55AA,0);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(layout==null)layout=new UiLayout(width,height);
        mouseXCache=mx;mouseYCache=my;
        if(profileModal){
            if(button==0){int mw=Math.min(360,width-24),mx0=(width-mw)/2,my0=(height-226)/2,by=my0+192,bw=(mw-32)/4;
                if(inside(mx,my,mx0+12,my0+34,mw-24,24))return true;
                List<String> p=CloverProfiles.list();for(int i=0;i<Math.min(4,p.size());i++)if(inside(mx,my,mx0+12,my0+66+i*21,mw-24,19)){selectedProfile=p.get(i);profileName=selectedProfile;return true;}
                if(inside(mx,my,mx0+12,by,bw,23)){if(CloverProfiles.save(profileName))selectedProfile=profileName;return true;}
                if(inside(mx,my,mx0+14+bw,by,bw,23)){if(!selectedProfile.isEmpty()&&!profileName.equals(selectedProfile)&&CloverProfiles.rename(selectedProfile,profileName))selectedProfile=profileName;return true;}
                if(inside(mx,my,mx0+16+2*bw,by,bw,23)){if(!selectedProfile.isEmpty()&&CloverProfiles.load(selectedProfile)){profileModal=false;return true;}return true;}
                if(inside(mx,my,mx0+18+3*bw,by,bw,23)){if(!selectedProfile.isEmpty()&&CloverProfiles.delete(selectedProfile))selectedProfile="";return true;}
            }return true;
        }
        if(themeDropdown){if(button==0){int x=clamp(themeMenuX,4,Math.max(4,width-194)),y=layout.y+48;for(int i=0;i<Theme.Preset.values().length;i++){int col=i/10,row=i%10;if(inside(mx,my,x+4+col*92,y+3+row*18,92,18)){Theme.select(Theme.Preset.values()[i]);ModConfig.accentTheme=i;ModConfig.accentHex=String.format(java.util.Locale.ROOT,"#%06X",Theme.accentColor()&0xFFFFFF);ModConfig.save();themeDropdown=false;return true;}}}themeDropdown=false;return true;}
        if(profileDropdown){if(button==0){int x=layout.centerX+110,y=layout.y+48;if(inside(mx,my,x,y+22,130,20)){profileModal=true;profileDropdown=false;return true;}if(inside(mx,my,x,y+40,130,20)){profileModal=true;profileName=selectedProfile;profileDropdown=false;return true;}if(inside(mx,my,x,y+58,130,20)){if(!selectedProfile.isEmpty())CloverProfiles.delete(selectedProfile);profileDropdown=false;return true;}}profileDropdown=false;return true;}
        if(button==0){
            for(int i=0;i<TABS.length;i++){int ty=layout.sidebarViewportY+i*(layout.tabHeight+4)-sidebarScroll;if(inside(mx,my,layout.sidebarViewportX,ty,layout.sidebarViewportWidth,layout.tabHeight)){tabScrollOffsets[selectedTab]=scrollOffset;selectedTab=i;selectedFeature=-1;scrollOffset=tabScrollOffsets[i];refreshFeatures();calculateRightContent();return true;}}
            int right=layout.compact?layout.x+layout.width-GAP:layout.settingsX+layout.settingsWidth,top=layout.y+GAP;
            if(inside(mx,my,right-30,top+3,26,26)){close();return true;}
            if(inside(mx,my,right-60,top+3,26,26)){close();return true;}
            int searchW=Math.max(90,Math.min((right-layout.centerX)/3,(right-layout.centerX)-160));
            if(inside(mx,my,layout.centerX+6,top+5,Math.max(90,searchW),26)){searchFocused=true;return true;}
            int profileX=profileMenuX;
            if(inside(mx,my,profileX,top+5,118,26)){profileDropdown=true;return true;}
            if(inside(mx,my,themeMenuX,top+5,Math.max(30,right-30-themeMenuX-8),26)){themeDropdown=true;return true;}
            int vx=layout.centerViewportX,vy=layout.centerViewportY,vw=layout.centerViewportWidth,vh=layout.centerViewportHeight;
            if(listContentHeight>vh&&inside(mx,my,vx+vw-5,vy,8,vh)){
                int thumb=Math.max(18,vh*vh/listContentHeight),track=Math.max(1,vh-thumb),max=Math.max(0,listContentHeight-vh);
                int thumbTop=vy+(vh-thumb)*scrollOffset/max;
                if(my<thumbTop)scrollOffset=Math.max(0,scrollOffset-layout.centerViewportHeight);
                else if(my>thumbTop+thumb)scrollOffset=Math.min(max,scrollOffset+layout.centerViewportHeight);
                else {int target=clamp((int)my-vy-thumb/2,0,track);scrollOffset=(int)Math.round((double)target/track*max);draggingListScrollbar=true;listDragStartY=(int)my;listDragStartOffset=scrollOffset;}
                return true;
            }
            if(inside(mx,my,vx,vy,vw,vh)){
                int idx=(int)((my-vy-10+scrollOffset)/ROW_STEP);
                if(idx>=0&&idx<visibleFeatures.size()){
                    pendingRow=idx;int toggleX=vx+vw-50,ry=vy+10+idx*ROW_STEP-scrollOffset;
                    pendingRowToggle=inside(mx,my,toggleX,ry+18,40,20);return true;
                }
            }
            if(!layout.compact){
                int rx=layout.settingsViewportX,ry=layout.settingsViewportY-settingsScroll,rw=layout.settingsViewportWidth;
                if(selectedFeature<0){
                    if(inside(mx,my,rx+12,ry+109,rw-24,16)){draggingSlider=0;setSliderValue(0,mx);return true;}
                    if(inside(mx,my,rx+12,ry+163,rw-24,16)){draggingSlider=1;setSliderValue(1,mx);return true;}
                    if(inside(mx,my,rx,ry+217,rw,28)){cycleTheme();return true;}
                    if(inside(mx,my,rx,ry+253,rw,24)){ModConfig.tintMenuBackground=!ModConfig.tintMenuBackground;Theme.setTintBackground(ModConfig.tintMenuBackground);ModConfig.save();return true;}
                    if(inside(mx,my,rx,ry+281,rw,22)){Theme.select(Theme.Preset.CLOVER);ModConfig.accentTheme=0;ModConfig.accentHex="#2BE36B";ModConfig.tintMenuBackground=true;Theme.setTintBackground(true);ModConfig.save();return true;}
                    if(inside(mx,my,rx,ry+328,rw,25)){
                        ModConfig.uiScale=(ModConfig.uiScale+1)%5;ModConfig.save();
                        applyMenuScale(MinecraftClient.getInstance());return true;
                    }
                    if(inside(mx,my,rx,ry+357,rw,24)){ModConfig.showBounds=!ModConfig.showBounds;ModConfig.save();return true;}
                }else if(inside(mx,my,rx+rw-42,ry+76,40,20)){CloverFeature f=visibleFeatures.get(selectedFeature);f.setEnabled(!f.enabled());return true;}
            }
        }
        if(button==1){int vx=layout.centerViewportX,vy=layout.centerViewportY,vw=layout.centerViewportWidth,vh=layout.centerViewportHeight;if(inside(mx,my,vx,vy,vw,vh)){int idx=(int)((my-vy+scrollOffset)/ROW_HEIGHT);if(idx>=0&&idx<visibleFeatures.size()){CloverFeature f=visibleFeatures.get(idx);f.setEnabled(!f.enabled());return true;}}}
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(layout==null)layout=new UiLayout(width,height);
        if(inside(mx,my,layout.sidebarViewportX,layout.sidebarViewportY,layout.sidebarViewportWidth,layout.sidebarViewportHeight)){int max=Math.max(0,TABS.length*(layout.tabHeight+4)-layout.sidebarViewportHeight);sidebarScroll=clamp(sidebarScroll-(int)Math.round(vertical*28),0,max);return true;}
        if(inside(mx,my,layout.centerViewportX,layout.centerViewportY,layout.centerViewportWidth,layout.centerViewportHeight)){int max=Math.max(0,listContentHeight-layout.centerViewportHeight);scrollOffset=clamp(scrollOffset-(int)Math.round(vertical*28),0,max);tabScrollOffsets[selectedTab]=scrollOffset;return true;}
        if(!layout.compact&&inside(mx,my,layout.settingsViewportX,layout.settingsViewportY,layout.settingsViewportWidth,layout.settingsViewportHeight)){settingsScroll=clamp(settingsScroll-(int)(vertical*24),0,Math.max(0,rightContentHeight-layout.settingsViewportHeight));return true;}
        return super.mouseScrolled(mx,my,horizontal,vertical);
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        mouseXCache=mx;mouseYCache=my;
        if(button==0&&draggingSlider>=0){setSliderValue(draggingSlider,mx);return true;}
        if(button==0&&draggingListScrollbar){
            int vh=layout.centerViewportHeight,thumb=Math.max(18,vh*vh/Math.max(1,listContentHeight)),track=Math.max(1,vh-thumb);
            int max=Math.max(0,listContentHeight-vh);
            scrollOffset=clamp(listDragStartOffset+(int)Math.round((my-listDragStartY)*(double)max/track),0,max);return true;
        }
        if(button==0&&inside(pressStartX,pressStartY,layout.centerViewportX,layout.centerViewportY,layout.centerViewportWidth,layout.centerViewportHeight)
                &&Math.abs(my-pressStartY)>6){
            touchScrolling=true;int max=Math.max(0,listContentHeight-layout.centerViewportHeight);
            scrollOffset=clamp((int)Math.round(scrollOffset-(my-pressStartY)),0,max);pressStartY=my;return true;
        }
        return super.mouseDragged(mx,my,button,dx,dy);
    }
    @Override public boolean mouseReleased(double mx,double my,int button){
        if(button==0&&draggingSlider>=0){draggingSlider=-1;return true;}
        if(button==0&&draggingListScrollbar){draggingListScrollbar=false;return true;}
        if(button==0){
            if(!touchScrolling&&pendingRow>=0&&pendingRow<visibleFeatures.size()){
                selectedFeature=pendingRow;calculateRightContent();
                if(pendingRowToggle){CloverFeature feature=visibleFeatures.get(pendingRow);feature.setEnabled(!feature.enabled());}
            }
            pendingRow=-1;pendingRowToggle=false;
            if(touchScrolling){touchScrolling=false;return true;}
        }
        return super.mouseReleased(mx,my,button);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(profileModal){if(key==GLFW.GLFW_KEY_ESCAPE){profileModal=false;return true;}if(key==GLFW.GLFW_KEY_BACKSPACE&&!profileName.isEmpty()){profileName=profileName.substring(0,profileName.length()-1);return true;}if(key==GLFW.GLFW_KEY_ENTER){if(CloverProfiles.save(profileName))selectedProfile=profileName;return true;}}
        if(searchFocused){if(key==GLFW.GLFW_KEY_ESCAPE){searchFocused=false;return true;}if(key==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()){search=search.substring(0,search.length()-1);refreshFeatures();return true;}if(key==GLFW.GLFW_KEY_ENTER){searchFocused=false;return true;}}
        if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}
        if(layout!=null){int max=Math.max(0,listContentHeight-layout.centerViewportHeight);if(key==GLFW.GLFW_KEY_UP){scrollOffset=clamp(scrollOffset-28,0,max);return true;}if(key==GLFW.GLFW_KEY_DOWN){scrollOffset=clamp(scrollOffset+28,0,max);return true;}if(key==GLFW.GLFW_KEY_PAGE_UP){scrollOffset=clamp(scrollOffset-layout.centerViewportHeight,0,max);return true;}if(key==GLFW.GLFW_KEY_PAGE_DOWN){scrollOffset=clamp(scrollOffset+layout.centerViewportHeight,0,max);return true;}if(key==GLFW.GLFW_KEY_HOME){scrollOffset=0;return true;}if(key==GLFW.GLFW_KEY_END){scrollOffset=max;return true;}}
        return super.keyPressed(key,scan,mods);
    }
    @Override public boolean charTyped(char chr,int modifiers){if(profileModal&&!Character.isISOControl(chr)&&profileName.length()<32){profileName+=chr;return true;}if(searchFocused&&!Character.isISOControl(chr)&&search.length()<48){search+=chr;refreshFeatures();return true;}return super.charTyped(chr,modifiers);}
}
