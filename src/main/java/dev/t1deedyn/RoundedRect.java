package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Allocation-free integer scanline rounded rectangles with analytic edge coverage. */
public final class RoundedRect {
    private static final int MAX_RADIUS=32;
    private RoundedRect(){}
    private static int withAlpha(int color,int coverage){
        int a=(color>>>24)&255;
        return (color&0x00FFFFFF)|((a*coverage/255)<<24);
    }
    public static void fill(DrawContext c,int x,int y,int w,int h,int radius,int color){
        if(w<=0||h<=0)return;
        int r=Math.max(0,Math.min(MAX_RADIUS,Math.min(radius,Math.min(w,h)/2)));
        if(r==0){c.fill(x,y,x+w,y+h,color);return;}
        double rr=(double)r*r;
        for(int row=0;row<h;row++){
            int dy=row<r?r-row-1:row>=h-r?row-(h-r):0;
            int inset=0,coverage=255;
            if(dy>0){
                double root=Math.sqrt(Math.max(0.0,rr-(double)dy*dy));
                double exact=r-root;
                inset=(int)Math.floor(exact);
                coverage=(int)Math.round((1.0-(exact-inset))*255.0);
            }
            if(row>=r&&row<h-r)inset=0;
            int left=x+inset,right=x+w-inset;
            if(right<=left)continue;
            if(coverage<255){
                c.fill(left,y+row,left+1,y+row+1,withAlpha(color,coverage));
                if(right-left>1)c.fill(right-1,y+row,right,y+row+1,withAlpha(color,coverage));
                left++;right--;
            }
            if(right>left)c.fill(left,y+row,right,y+row+1,color);
        }
    }
    public static void border(DrawContext c,int x,int y,int w,int h,int radius,int thickness,int outer,int inner){
        if(w<=0||h<=0||thickness<=0)return;
        fill(c,x,y,w,h,radius,outer);
        int iw=w-thickness*2,ih=h-thickness*2;
        if(iw>0&&ih>0)fill(c,x+thickness,y+thickness,iw,ih,Math.max(0,radius-thickness),inner);
    }
}
