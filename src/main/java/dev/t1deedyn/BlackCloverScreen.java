package dev.t1deedyn;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Main Black Clover interface. Widget geometry is shared by rendering and input. */
public final class BlackCloverScreen extends Screen {
    private static final String[] TABS={"Визуал","Игрок","Мир","Интерфейс","HUD","Эффекты","Настройки"};
    private static final String[] ICONS={"◉","♟","◎","▣","▥","✦","⚙"};
    private static final String[] DESCRIPTIONS={
        "Настройка визуальных эффектов и отображения","Настройки игрока и автоматизации",
        "Окружение, погода и подсветка мира","Масштаб, прозрачность и оформление",
        "Информация и элементы игрового HUD","Частицы и визуальные эффекты",
        "Профили, производительность и информация"
    };
    private static final int ROW_HEIGHT=54;
    private static final int LIST_TOP=72;
    private final List<CloverFeature> visibleFeatures=new ArrayList<>();
    private UiLayout layout;
    private int selectedTab;
    private int selectedFeature=-1;
    private int scrollOffset;
    private boolean searchFocused;
    private boolean minimized;
    private String search="";
    private int hoverRow=-1;

    public BlackCloverScreen(){super(Text.literal("Чёрный Клевер"));}
    @Override protected void init(){layout=new UiLayout(width,height);refreshFeatures();}
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext c,int mouseX,int mouseY,float delta){c.fill(0,0,width,height,0x59000000);}

    private void refreshFeatures(){
        visibleFeatures.clear();
        String query=search.trim().toLowerCase(java.util.Locale.ROOT);
        for(CloverFeature feature:CloverFeature.ALL){
            if(query.isEmpty()&&feature.tab!=selectedTab)continue;
            if(!query.isEmpty()&&!feature.name.toLowerCase(java.util.Locale.ROOT).contains(query)
                    &&!feature.description.toLowerCase(java.util.Locale.ROOT).contains(query))continue;
            visibleFeatures.add(feature);
        }
        int max=Math.max(0,visibleFeatures.size()*ROW_HEIGHT-Math.max(0,layout==null?180:layout.contentHeight-LIST_TOP-10));
        scrollOffset=Math.max(0,Math.min(scrollOffset,max));
        if(selectedFeature>=visibleFeatures.size())selectedFeature=-1;
    }

    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){
        renderBackground(c,mouseX,mouseY,delta);
        if(layout==null)layout=new UiLayout(width,height);
        int x=layout.x,y=layout.y,w=layout.width,h=layout.height;
        int accent=Theme.accentColor(),bright=Theme.brightAccentColor();
        RoundedRect.fill(c,x-3,y-3,w+6,h+6,17,0x182BE36B);
        RoundedRect.border(c,x,y,w,h,UiLayout.WINDOW_RADIUS,1,bright,Theme.BG);
        RoundedRect.border(c,layout.sidebarX,y+UiLayout.GAP,layout.sidebarWidth,h-UiLayout.GAP*2,10,1,Theme.BORDER,Theme.PANEL);
        int logoX=layout.sidebarX+(layout.sidebarWidth-64)/2;
        CloverMark.draw(c,logoX,y+22,64,accent);
        drawCentered(c,"ЧЁРНЫЙ",layout.sidebarX+layout.sidebarWidth/2,y+91,Theme.TEXT);
        drawCentered(c,"КЛЕВЕР",layout.sidebarX+layout.sidebarWidth/2,y+103,Theme.TEXT);
        int tabY=y+132;
        for(int i=0;i<TABS.length;i++){
            int ty=tabY+i*36;
            boolean hovered=inside(mouseX,mouseY,layout.sidebarX+6,ty,layout.sidebarWidth-12,32);
            if(i==selectedTab)RoundedRect.border(c,layout.sidebarX+6,ty,layout.sidebarWidth-12,32,8,1,bright,0x702BE36B);
            else if(hovered)RoundedRect.fill(c,layout.sidebarX+6,ty,layout.sidebarWidth-12,32,8,Theme.CARD_HOVER);
            c.drawText(textRenderer,ICONS[i],layout.sidebarX+15,ty+10,i==selectedTab?Theme.TEXT:Theme.TEXT_DIM,false);
            c.drawText(textRenderer,TABS[i],layout.sidebarX+36,ty+11,i==selectedTab?Theme.TEXT:Theme.TEXT_DIM,false);
        }
        int footerY=y+h-39;
        c.fill(layout.sidebarX+10,footerY-8,layout.sidebarX+layout.sidebarWidth-10,footerY-7,Theme.BORDER);
        c.drawText(textRenderer,"✦ Minecraft 1.21.0",layout.sidebarX+10,footerY,Theme.TEXT_DIM,false);
        c.drawText(textRenderer,"Fabric",layout.sidebarX+10,footerY+12,accent,false);

        int mainX=layout.centerX;
        int mainRight=layout.compact?x+w-UiLayout.GAP:layout.settingsX+layout.settingsWidth;
        int mainWidth=mainRight-mainX;
        RoundedRect.border(c,mainX,y+UiLayout.GAP,mainWidth,UiLayout.TOPBAR_HEIGHT,8,1,Theme.BORDER,Theme.PANEL);
        int searchW=Math.max(70,mainWidth/3);
        RoundedRect.fill(c,mainX+7,y+UiLayout.GAP+5,searchW,26,7,Theme.CARD);
        c.drawText(textRenderer,"⌕",mainX+15,y+UiLayout.GAP+13,Theme.TEXT_DIM,false);
        String searchLabel=search.isEmpty()?"Поиск функций...":search;
        c.drawText(textRenderer,searchLabel,mainX+31,y+UiLayout.GAP+13,searchFocused?Theme.TEXT:Theme.TEXT_DIM,false);
        int controlsX=mainX+searchW+18;
        if(mainWidth>330)c.drawText(textRenderer,"♣ Профиль 1",controlsX,y+UiLayout.GAP+14,Theme.TEXT,false);
        if(mainWidth>440)c.drawText(textRenderer,"Тема ⌄",Math.min(mainRight-110,controlsX+95),y+UiLayout.GAP+14,accent,false);
        RoundedRect.fill(c,mainRight-66,y+UiLayout.GAP+4,28,28,7,Theme.CARD);
        RoundedRect.fill(c,mainRight-34,y+UiLayout.GAP+4,28,28,7,Theme.CARD);
        drawCentered(c,"—",mainRight-52,y+UiLayout.GAP+13,Theme.TEXT);
        drawCentered(c,"×",mainRight-20,y+UiLayout.GAP+13,Theme.TEXT);

        int contentY=layout.contentY,contentH=layout.contentHeight;
        int centerW=layout.compact?mainWidth:layout.centerWidth;
        RoundedRect.border(c,mainX,contentY,centerW,contentH,9,1,Theme.BORDER,Theme.PANEL);
        CloverMark.draw(c,mainX+12,contentY+12,30,accent);
        c.drawText(textRenderer,search.isEmpty()?TABS[selectedTab].toUpperCase(java.util.Locale.ROOT):"РЕЗУЛЬТАТЫ ПОИСКА",mainX+52,contentY+15,accent,false);
        if(!layout.compact)c.drawText(textRenderer,DESCRIPTIONS[selectedTab],mainX+12,contentY+51,Theme.TEXT_DIM,false);
        refreshFeatures();
        int listX=mainX+8,listY=contentY+LIST_TOP,listW=centerW-16;
        int listH=Math.max(30,contentH-LIST_TOP-8);
        c.enableScissor(listX,listY,listX+listW,listY+listH);
        hoverRow=-1;
        for(int i=0;i<visibleFeatures.size();i++){
            int rowY=listY+i*ROW_HEIGHT-scrollOffset;
            if(rowY+ROW_HEIGHT<listY||rowY>listY+listH)continue;
            CloverFeature feature=visibleFeatures.get(i);
            boolean hovered=inside(mouseX,mouseY,listX,rowY,listW,ROW_HEIGHT-4);
            if(hovered)hoverRow=i;
            if(i==selectedFeature)RoundedRect.border(c,listX,rowY,listW,ROW_HEIGHT-4,8,1,bright,Theme.CARD);
            else if(hovered)RoundedRect.fill(c,listX,rowY,listW,ROW_HEIGHT-4,8,Theme.CARD_HOVER);
            else RoundedRect.fill(c,listX,rowY,listW,ROW_HEIGHT-4,8,Theme.CARD);
            c.fill(listX+9,rowY+10,listX+11,rowY+ROW_HEIGHT-14,i==selectedFeature?accent:Theme.BORDER);
            c.drawText(textRenderer,feature.name,listX+18,rowY+10,Theme.TEXT,false);
            if(!layout.compact)c.drawText(textRenderer,feature.description,listX+18,rowY+27,Theme.TEXT_DIM,false);
            drawToggle(c,listX+listW-52,rowY+16,feature.enabled(),accent);
        }
        c.disableScissor();
        if(visibleFeatures.isEmpty())drawCentered(c,"Ничего не найдено",mainX+centerW/2,contentY+contentH/2,Theme.TEXT_DIM);
        if(!layout.compact){
            RoundedRect.border(c,layout.settingsX,contentY,layout.settingsWidth,contentH,9,1,Theme.BORDER,Theme.PANEL);
            CloverMark.draw(c,layout.settingsX+12,contentY+12,28,accent);
            c.drawText(textRenderer,"НАСТРОЙКИ",layout.settingsX+48,contentY+18,accent,false);
            if(selectedFeature>=0&&selectedFeature<visibleFeatures.size()){
                CloverFeature feature=visibleFeatures.get(selectedFeature);
                c.drawText(textRenderer,feature.name,layout.settingsX+12,contentY+58,Theme.TEXT,false);
                c.drawText(textRenderer,feature.description,layout.settingsX+12,contentY+78,Theme.TEXT_DIM,false);
                c.drawText(textRenderer,"Состояние",layout.settingsX+12,contentY+116,Theme.TEXT_DIM,false);
                drawToggle(c,layout.settingsX+layout.settingsWidth-58,contentY+110,feature.enabled(),accent);
                c.drawText(textRenderer,feature.enabled()?"Включено":"Выключено",layout.settingsX+12,contentY+137,feature.enabled()?accent:Theme.TEXT_DIM,false);
            }else drawCentered(c,"Выберите функцию",layout.settingsX+layout.settingsWidth/2,contentY+contentH/2,Theme.TEXT_DIM);
        }
        int tagW=Math.min(190,w-20),tagX=x+w-tagW,tagY=y+h+5;
        RoundedRect.border(c,tagX,tagY,tagW,22,8,1,bright,Theme.PANEL);
        CloverMark.draw(c,tagX+6,tagY+4,14,accent);
        c.drawText(textRenderer,"T1deedyn & PvE_BoTiK",tagX+25,tagY+7,bright,false);
    }

    private void drawToggle(DrawContext c,int x,int y,boolean enabled,int accent){
        RoundedRect.fill(c,x,y,40,20,10,enabled?accent:Theme.TOGGLE_OFF);
        int knobX=enabled?x+22:x+2;
        RoundedRect.fill(c,knobX,y+2,16,16,8,0xFFFFFFFF);
    }

    private void drawCentered(DrawContext c,String value,int centerX,int y,int color){
        c.drawText(textRenderer,value,centerX-textRenderer.getWidth(value)/2,y,color,false);
    }
    private static boolean inside(double mx,double my,int x,int y,int w,int h){
        return mx>=x&&mx<x+w&&my>=y&&my<y+h;
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(layout==null)layout=new UiLayout(width,height);
        int x=layout.x,y=layout.y;
        if(button==0||button==1){
            int tabY=y+132;
            if(button==0){
                for(int i=0;i<TABS.length;i++){
                    int ty=tabY+i*36;
                    if(inside(mx,my,layout.sidebarX+6,ty,layout.sidebarWidth-12,32)){
                        selectedTab=i;scrollOffset=0;selectedFeature=-1;refreshFeatures();return true;
                    }
                }
            }
            int right=layout.compact?x+layout.width-UiLayout.GAP:layout.settingsX+layout.settingsWidth;
            int topY=y+UiLayout.GAP;
            if(button==0&&inside(mx,my,right-66,topY+4,28,28)){minimized=true;close();return true;}
            if(button==0&&inside(mx,my,right-34,topY+4,28,28)){close();return true;}
            int searchW=Math.max(70,(right-layout.centerX)/3);
            if(button==0&&inside(mx,my,layout.centerX+7,topY+5,searchW,26)){searchFocused=true;return true;}
            int centerW=layout.compact?(right-layout.centerX):layout.centerWidth;
            int listX=layout.centerX+8,listY=layout.contentY+LIST_TOP,listW=centerW-16;
            int listH=Math.max(30,layout.contentHeight-LIST_TOP-8);
            if(inside(mx,my,listX,listY,listW,listH)){
                int index=(int)((my-listY+scrollOffset)/ROW_HEIGHT);
                if(index>=0&&index<visibleFeatures.size()){
                    selectedFeature=index;
                    CloverFeature feature=visibleFeatures.get(index);
                    if(button==1){feature.setEnabled(!feature.enabled());return true;}
                    int toggleX=listX+listW-52,toggleY=listY+index*ROW_HEIGHT-scrollOffset+16;
                    if(inside(mx,my,toggleX,toggleY,40,20))feature.setEnabled(!feature.enabled());
                    return true;
                }
            }
            if(button==0&&!layout.compact&&selectedFeature>=0&&selectedFeature<visibleFeatures.size()){
                int sx=layout.settingsX+layout.settingsWidth-58,sy=layout.contentY+110;
                if(inside(mx,my,sx,sy,40,20)){
                    CloverFeature feature=visibleFeatures.get(selectedFeature);
                    feature.setEnabled(!feature.enabled());return true;
                }
            }
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){
        if(layout==null)layout=new UiLayout(width,height);
        int centerW=layout.compact?layout.width-(layout.centerX-layout.x)-UiLayout.GAP:layout.centerWidth;
        int listX=layout.centerX+8,listY=layout.contentY+LIST_TOP;
        if(inside(mouseX,mouseY,listX,listY,centerW-16,Math.max(30,layout.contentHeight-LIST_TOP-8))){
            int max=Math.max(0,visibleFeatures.size()*ROW_HEIGHT-(layout.contentHeight-LIST_TOP-8));
            scrollOffset=Math.max(0,Math.min(max,scrollOffset-(int)(verticalAmount*ROW_HEIGHT*2)));
            return true;
        }
        return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
    }

    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(searchFocused){
            if(keyCode==GLFW.GLFW_KEY_ESCAPE){searchFocused=false;return true;}
            if(keyCode==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()){search=search.substring(0,search.length()-1);scrollOffset=0;refreshFeatures();return true;}
            if(keyCode==GLFW.GLFW_KEY_ENTER){searchFocused=false;return true;}
        }
        if(keyCode==GLFW.GLFW_KEY_ESCAPE||keyCode==GLFW.GLFW_KEY_X){close();return true;}
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override public boolean charTyped(char chr,int modifiers){
        if(searchFocused&&!Character.isISOControl(chr)&&search.length()<48){search+=chr;scrollOffset=0;refreshFeatures();return true;}
        return super.charTyped(chr,modifiers);
    }
}
