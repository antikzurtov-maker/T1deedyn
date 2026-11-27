package dev.t1deedyn;

/** Integer-only three-column layout for Minecraft 1.21.0 GUI coordinates. */
public final class UiLayout {
    public static final int GAP=8, TOPBAR_HEIGHT=36, WINDOW_RADIUS=14, PANEL_RADIUS=9;
    public final int x,y,width,height,sidebarX,sidebarWidth,centerX,centerWidth,settingsX,settingsWidth,contentY,contentHeight;
    public final int logoHeight,tabTop,tabHeight,footerY,footerHeight,creatorY;
    public final int sidebarViewportX,sidebarViewportY,sidebarViewportWidth,sidebarViewportHeight;
    public final int centerViewportX,centerViewportY,centerViewportWidth,centerViewportHeight;
    public final int settingsViewportX,settingsViewportY,settingsViewportWidth,settingsViewportHeight;
    public final boolean compact;

    public UiLayout(int screenWidth,int screenHeight){
        int usableHeight=Math.max(1,screenHeight-28);
        width=Math.max(1,Math.min(screenWidth-16,Math.round(screenWidth*0.78f)));
        height=Math.max(1,Math.min(usableHeight-8,Math.round(usableHeight*0.74f)));
        x=Math.max(0,(screenWidth-width)/2);
        y=Math.max(0,(usableHeight-height)/2);
        creatorY=y+height+5;
        compact=width<440;
        int available=Math.max(1,width-GAP*4);
        if(compact){
            sidebarWidth=Math.max(88,Math.min(112,available*24/100));
            centerWidth=Math.max(1,available-sidebarWidth-GAP);
            settingsWidth=0;
        }else{
            sidebarWidth=Math.max(88,available*20/96);
            centerWidth=Math.max(1,available*43/96);
            settingsWidth=Math.max(1,available-sidebarWidth-centerWidth-GAP*2);
        }
        sidebarX=x+GAP;
        centerX=sidebarX+sidebarWidth+GAP;
        settingsX=centerX+centerWidth+GAP;
        contentY=y+TOPBAR_HEIGHT+GAP;
        contentHeight=Math.max(1,height-TOPBAR_HEIGHT-GAP*2);
        logoHeight=Math.min(104,Math.max(66,(int)(contentHeight*0.30)));
        footerHeight=18;
        footerY=y+height-GAP-footerHeight;
        tabTop=y+GAP+logoHeight+6;
        int tabZoneBottom=footerY-7;
        sidebarViewportX=sidebarX+5;
        sidebarViewportY=tabTop;
        sidebarViewportWidth=Math.max(1,sidebarWidth-10);
        sidebarViewportHeight=Math.max(1,tabZoneBottom-tabTop);
        tabHeight=Math.max(24,Math.min(30,(sidebarViewportHeight-6*4)/7));
        centerViewportX=centerX+10;
        centerViewportY=contentY+64;
        centerViewportWidth=Math.max(1,centerWidth-30);
        centerViewportHeight=Math.max(1,contentY+contentHeight-10-centerViewportY);
        settingsViewportX=settingsX+10;
        settingsViewportY=contentY+42;
        settingsViewportWidth=Math.max(1,settingsWidth-30);
        settingsViewportHeight=Math.max(1,contentY+contentHeight-10-settingsViewportY);
    }
    public boolean contains(int px,int py){return px>=x&&py>=y&&px<x+width&&py<y+height;}
    public static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
}
