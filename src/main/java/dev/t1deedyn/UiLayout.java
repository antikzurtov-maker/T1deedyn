package dev.t1deedyn;

/** Shared integer layout grid for the Black Clover screen. */
public final class UiLayout {
    public static final int GAP=8, TOPBAR_HEIGHT=36, MIN_WIDTH=520, MIN_HEIGHT=300;
    public static final int WINDOW_RADIUS=14, PANEL_RADIUS=9;
    public final int x,y,width,height,sidebarX,sidebarWidth,centerX,centerWidth,settingsX,settingsWidth,contentY,contentHeight;
    public final boolean compact;
    public UiLayout(int screenWidth,int screenHeight){
        width=Math.min(Math.max(1,screenWidth-16),Math.max(MIN_WIDTH,Math.round(screenWidth*0.78f)));
        height=Math.min(Math.max(1,screenHeight-16),Math.max(MIN_HEIGHT,Math.round(screenHeight*0.78f)));
        x=Math.max(0,(screenWidth-width)/2); y=Math.max(0,(screenHeight-height)/2);
        compact=width<560;
        int inner=width-GAP*4;
        sidebarWidth=Math.max(104,inner*20/96);
        centerWidth=compact?inner-sidebarWidth-GAP:inner*43/96;
        settingsWidth=compact?0:Math.max(120,inner-sidebarWidth-centerWidth-GAP*2);
        sidebarX=x+GAP; centerX=sidebarX+sidebarWidth+GAP;
        settingsX=compact?centerX:centerX+centerWidth+GAP;
        contentY=y+TOPBAR_HEIGHT+GAP; contentHeight=height-TOPBAR_HEIGHT-GAP*2;
    }
    public boolean contains(int px,int py){return px>=x&&py>=y&&px<x+width&&py<y+height;}
}
