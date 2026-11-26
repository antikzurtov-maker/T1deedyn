package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Black Clover interface shell. All geometry uses integer GUI coordinates. */
public final class BlackCloverScreen extends Screen {
    private static final String[] TABS={"Визуал","Игрок","Мир","Интерфейс","HUD","Эффекты","Настройки"};
    private static final String[] ICONS={"◉","♟","◎","▣","▥","✦","⚙"};
    private static final String[] DESCRIPTIONS={
        "Настройка визуальных эффектов и отображения",
        "Настройки игрока и автоматизации",
        "Окружение, погода и подсветка мира",
        "Масштаб, прозрачность и оформление",
        "Информация и элементы игрового HUD",
        "Частицы и визуальные эффекты",
        "Профили, отладка и информация о сборке"
    };
    private UiLayout layout;
    private int selectedTab;
    private boolean searchFocused;
    private boolean minimized;
    private String search="";
    private int hoverTab=-1;

    public BlackCloverScreen(){super(Text.literal("Чёрный Клевер"));}

    @Override protected void init(){layout=new UiLayout(width,height);}

    @Override public boolean shouldPause(){return false;}

    @Override public void renderBackground(DrawContext c,int mouseX,int mouseY,float delta){
        c.fill(0,0,width,height,0x59000000);
    }

    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta){
        renderBackground(c,mouseX,mouseY,delta);
        if(layout==null)layout=new UiLayout(width,height);
        int x=layout.x,y=layout.y,w=layout.width,h=layout.height;
        int accent=Theme.accentColor(),bright=Theme.brightAccentColor();
        RoundedRect.fill(c,x-3,y-3,w+6,h+6,17,0x182BE36B);
        RoundedRect.border(c,x,y,w,h,UiLayout.WINDOW_RADIUS,1,bright,Theme.BG);
        // Sidebar
        RoundedRect.border(c,layout.sidebarX,y+UiLayout.GAP,layout.sidebarWidth,h-UiLayout.GAP*2,10,1,Theme.BORDER,Theme.PANEL);
        int logoX=layout.sidebarX+(layout.sidebarWidth-64)/2;
        CloverMark.draw(c,logoX,y+22,64,accent);
        drawCentered(c,"ЧЁРНЫЙ",layout.sidebarX+layout.sidebarWidth/2,y+91,Theme.TEXT);
        drawCentered(c,"КЛЕВЕР",layout.sidebarX+layout.sidebarWidth/2,y+103,Theme.TEXT);
        int tabY=y+132;
        hoverTab=-1;
        for(int i=0;i<TABS.length;i++){
            int ty=tabY+i*36;
            boolean hovered=mouseX>=layout.sidebarX+6&&mouseX<layout.sidebarX+layout.sidebarWidth-6&&mouseY>=ty&&mouseY<ty+32;
            if(hovered)hoverTab=i;
            if(i==selectedTab)RoundedRect.border(c,layout.sidebarX+6,ty,layout.sidebarWidth-12,32,8,1,bright,0x702BE36B);
            else if(hovered)RoundedRect.fill(c,layout.sidebarX+6,ty,layout.sidebarWidth-12,32,8,Theme.CARD_HOVER);
            c.drawText(textRenderer,ICONS[i],layout.sidebarX+15,ty+10,i==selectedTab?Theme.TEXT:Theme.TEXT_DIM,false);
            c.drawText(textRenderer,TABS[i],layout.sidebarX+36,ty+11,i==selectedTab?Theme.TEXT:Theme.TEXT_DIM,false);
        }
        int footerY=y+h-39;
        c.fill(layout.sidebarX+10,footerY-8,layout.sidebarX+layout.sidebarWidth-10,footerY-7,Theme.BORDER);
        c.drawText(textRenderer,"✦ Minecraft 1.21.0",layout.sidebarX+10,footerY,Theme.TEXT_DIM,false);
        c.drawText(textRenderer,"Fabric",layout.sidebarX+10,footerY+12,accent,false);

        // Top bar and main content panels.
        int mainX=layout.centerX;
        int mainRight=layout.compact?x+w-UiLayout.GAP:layout.settingsX+layout.settingsWidth;
        int mainWidth=mainRight-mainX;
        RoundedRect.border(c,mainX,y+UiLayout.GAP,mainWidth,UiLayout.TOPBAR_HEIGHT,8,1,Theme.BORDER,Theme.PANEL);
        RoundedRect.fill(c,mainX+7,y+UiLayout.GAP+5,Math.max(70,mainWidth/3),26,7,Theme.CARD);
        c.drawText(textRenderer,"⌕",mainX+15,y+UiLayout.GAP+13,Theme.TEXT_DIM,false);
        c.drawText(textRenderer,search.isEmpty()?"Поиск функций...":search,mainX+31,y+UiLayout.GAP+13,searchFocused?Theme.TEXT:Theme.TEXT_DIM,false);
        int controlsX=mainX+Math.max(80,mainWidth/3)+18;
        c.drawText(textRenderer,"♣ Профиль 1",controlsX,y+UiLayout.GAP+14,Theme.TEXT,false);
        c.drawText(textRenderer,"Тема ⌄",Math.min(mainRight-110,controlsX+95),y+UiLayout.GAP+14,accent,false);
        RoundedRect.fill(c,mainRight-66,y+UiLayout.GAP+4,28,28,7,Theme.CARD);
        RoundedRect.fill(c,mainRight-34,y+UiLayout.GAP+4,28,28,7,Theme.CARD);
        drawCentered(c,"—",mainRight-52,y+UiLayout.GAP+13,Theme.TEXT);
        drawCentered(c,"×",mainRight-20,y+UiLayout.GAP+13,Theme.TEXT);

        int contentY=layout.contentY;
        int contentH=layout.contentHeight;
        int centerW=layout.compact?mainWidth:layout.centerWidth;
        RoundedRect.border(c,mainX,contentY,centerW,contentH,9,1,Theme.BORDER,Theme.PANEL);
        CloverMark.draw(c,mainX+12,contentY+12,30,accent);
        c.drawText(textRenderer,TABS[selectedTab].toUpperCase(java.util.Locale.ROOT),mainX+52,contentY+15,accent,false);
        c.drawText(textRenderer,DESCRIPTIONS[selectedTab],mainX+12,contentY+51,Theme.TEXT_DIM,false);
        drawCentered(c,"Список функций будет размещён здесь",mainX+centerW/2,contentY+contentH/2,Theme.TEXT_DIM);
        if(!layout.compact){
            RoundedRect.border(c,layout.settingsX,contentY,layout.settingsWidth,contentH,9,1,Theme.BORDER,Theme.PANEL);
            CloverMark.draw(c,layout.settingsX+12,contentY+12,28,accent);
            c.drawText(textRenderer,"НАСТРОЙКИ",layout.settingsX+48,contentY+18,accent,false);
            drawCentered(c,"Выберите функцию",layout.settingsX+layout.settingsWidth/2,contentY+contentH/2,Theme.TEXT_DIM);
        }
        // Creator tag, attached to the lower-right corner of the window.
        int tagW=Math.min(190,w-20),tagX=x+w-tagW,tagY=y+h+5;
        RoundedRect.border(c,tagX,tagY,tagW,22,8,1,bright,Theme.PANEL);
        CloverMark.draw(c,tagX+6,tagY+4,14,accent);
        c.drawText(textRenderer,"T1deedyn & PvE_BoTiK",tagX+25,tagY+7,bright,false);
    }

    private void drawCentered(DrawContext c,String value,int centerX,int y,int color){\n        c.drawText(textRenderer,value,centerX-textRenderer.getWidth(value)/2,y,color,false);\n    }\n\n    @Override public boolean mouseClicked(double mx,double my,int button){
        if(layout==null)layout=new UiLayout(width,height);
        int x=layout.x,y=layout.y;
        if(button==0){
            int tabY=y+132;
            for(int i=0;i<TABS.length;i++){
                int ty=tabY+i*36;
                if(mx>=layout.sidebarX+6&&mx<layout.sidebarX+layout.sidebarWidth-6&&my>=ty&&my<ty+32){selectedTab=i;return true;}
            }
            int right=layout.compact?x+layout.width-UiLayout.GAP:layout.settingsX+layout.settingsWidth;
            int topY=y+UiLayout.GAP;
            if(my>=topY&&my<topY+UiLayout.TOPBAR_HEIGHT){
                if(mx>=right-66&&mx<right-38){minimized=true;close();return true;}
                if(mx>=right-34&&mx<right-6){close();return true;}
                if(mx>=layout.centerX+7&&mx<layout.centerX+Math.max(70,(right-layout.centerX)/3)){searchFocused=true;return true;}
            }
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(searchFocused){
            if(keyCode==GLFW.GLFW_KEY_ESCAPE){searchFocused=false;return true;}
            if(keyCode==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()){search=search.substring(0,search.length()-1);return true;}
            if(keyCode==GLFW.GLFW_KEY_ENTER){searchFocused=false;return true;}
        }
        if(keyCode==GLFW.GLFW_KEY_ESCAPE||keyCode==GLFW.GLFW_KEY_X){close();return true;}
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override public boolean charTyped(char chr,int modifiers){
        if(searchFocused&&!Character.isISOControl(chr)&&search.length()<48){search+=chr;return true;}
        return super.charTyped(chr,modifiers);
    }
}
