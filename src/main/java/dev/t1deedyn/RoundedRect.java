package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Integer-coordinate rounded rectangles with precomputed row insets. */
public final class RoundedRect {
    private static final int MAX_RADIUS=32, DIAMETER=MAX_RADIUS*2+1;
    private static final int[][] INSETS=new int[MAX_RADIUS+1][DIAMETER];
    static {
        for(int r=0;r<=MAX_RADIUS;r++) for(int row=0;row<=r*2;row++){
            int dy=r-row, inset=0;
            if(r>0) inset=r-(int)Math.floor(Math.sqrt(Math.max(0.0,(double)r*r-(double)dy*dy)));
            INSETS[r][row]=inset;
        }
    }
    private RoundedRect(){}
    public static void fill(DrawContext c,int x,int y,int w,int h,int radius,int color){
        if(w<=0||h<=0)return;
        int r=Math.max(0,Math.min(MAX_RADIUS,Math.min(radius,Math.min(w,h)/2)));
        if(r==0){c.fill(x,y,x+w,y+h,color);return;}
        for(int row=0;row<h;row++){
            int inset=row<r?INSETS[r][row]:(row>=h-r?INSETS[r][h-1-row]:0);
            if(w>inset*2)c.fill(x+inset,y+row,x+w-inset,y+row+1,color);
        }
    }
    public static void border(DrawContext c,int x,int y,int w,int h,int radius,int thickness,int outer,int inner){
        if(w<=0||h<=0||thickness<=0)return;
        fill(c,x,y,w,h,radius,outer);
        int iw=w-thickness*2,ih=h-thickness*2;
        if(iw>0&&ih>0)fill(c,x+thickness,y+thickness,iw,ih,Math.max(0,radius-thickness),inner);
    }
}
