package dev.t1deedyn;

/** Integer-only layout. The creators strip is reserved outside the window. */
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
        compact=width<760;
        int inner=Math.max(1,width-GAP*4);
        sidebarWidth=Math.max(104,Math.min(190,inner*20/96));
        int remaining=Math.max(1,inner-sidebarWidth-GAP*2);
        centerWidth=compact?remaining:remaining*43/76;
        settingsWidth=compact?0:Math.max(1,remaining-centerWidth);
        sidebarX=x+GAP;
        centerX=sidebarX+sidebarWidth+GAP;
        settingsX=compact?centerX:centerX+centerWidth+GAP;
        contentY=y+TOPBAR_HEIGHT+GAP;
        contentHeight=Math.max(1,height-TOPBAR_HEIGHT-GAP*2);
        logoHeight=Math.min(90,Math.max(24,(int)(contentHeight*0.26)));
        footerHeight=18;
        footerY=y+height-GAP-footerHeight;
        tabTop=y+GAP+logoHeight+6;
        int tabZoneBottom=footerY-7;
        sidebarViewportX=sidebarX+5;
        sidebarViewportY=tabTop;
        sidebarViewportWidth=Math.max(1,sidebarWidth-10);
        sidebarViewportHeight=Math.max(1,tabZoneBottom-tabTop);
        tabHeight=Math.max(18,Math.min(30,(sidebarViewportHeight-6*4)/7));
        centerViewportX=centerX+8;
        centerViewportY=contentY+64;
        centerViewportWidth=Math.max(1,(compact?width-GAP*3:centerWidth)-16);
        centerViewportHeight=Math.max(1,contentY+contentHeight-8-centerViewportY);
        settingsViewportX=settingsX+8;
        settingsViewportY=contentY+42;
        settingsViewportWidth=Math.max(1,settingsWidth-16);
        settingsViewportHeight=Math.max(1,contentY+contentHeight-8-settingsViewportY);
    }
    public boolean contains(int px,int py){return px>=x&&py>=y&&px<x+width&&py<y+height;}
    public static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
}
