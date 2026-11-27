package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Cached 4x analytic edge coverage with integer-coordinate scanline rendering. */
public final class RoundedRect {
    private static final int MAX_RADIUS=32,SS=4;
    private static final int[][] EDGE_X=new int[MAX_RADIUS+1][MAX_RADIUS*2+1];
    private static final int[][] EDGE_A=new int[MAX_RADIUS+1][MAX_RADIUS*2+1];
    private static final int[][] FULL_X=new int[MAX_RADIUS+1][MAX_RADIUS*2+1];
    static {
        for(int r=0;r<=MAX_RADIUS;r++){
            for(int row=0;row<=r*2;row++){
                if(r==0){EDGE_X[r][row]=0;EDGE_A[r][row]=255;FULL_X[r][row]=0;continue;}
                int first=-1,alpha=0,full=0;
                for(int px=0;px<=r;px++){
                    int covered=0;
                    for(int sy=0;sy<SS;sy++)for(int sx=0;sx<SS;sx++){
                        double xx=px+(sx+0.5)/SS,yy=row+(sy+0.5)/SS;
                        double dx=r-xx,dy=r-yy;
                        if(dx*dx+dy*dy<=r*r)covered++;
                    }
                    int coverage=covered*255/(SS*SS);
                    if(coverage>0&&first<0){first=px;alpha=coverage;}
                    if(coverage==255)full=px;
                }
                if(first<0){first=0;alpha=0;}
                EDGE_X[r][row]=first;EDGE_A[r][row]=alpha;FULL_X[r][row]=Math.max(first,full);
            }
        }
    }
    private RoundedRect(){}
    private static int alpha(int color,int coverage){int a=(color>>>24)&255;return(color&0x00FFFFFF)|((a*coverage/255)<<24);}
    public static void fill(DrawContext c,int x,int y,int w,int h,int radius,int color){
        if(w<=0||h<=0)return;
        int r=Math.max(0,Math.min(MAX_RADIUS,Math.min(radius,Math.min(w,h)/2)));
        if(r==0){c.fill(x,y,x+w,y+h,color);return;}
        for(int row=0;row<h;row++){
            int edgeRow=row<r?row:(row>=h-r?h-1-row:r*2);
            int edge=EDGE_X[r][edgeRow],coverage=EDGE_A[r][edgeRow],full=FULL_X[r][edgeRow];
            int left=edge;
            if(coverage>0&&left<w/2)c.fill(x+left,y+row,x+left+1,y+row+1,alpha(color,coverage));
            int start=Math.max(left+1,full);
            int end=w-start;
            if(end>start)c.fill(x+start,y+row,x+end,y+row+1,color);
            if(coverage>0&&w-left-1>=w/2)c.fill(x+w-left-1,y+row,x+w-left,y+row+1,alpha(color,coverage));
        }
    }
    public static void border(DrawContext c,int x,int y,int w,int h,int radius,int thickness,int outer,int inner){
        if(w<=0||h<=0||thickness<=0)return;
        fill(c,x,y,w,h,radius,outer);
        int iw=w-thickness*2,ih=h-thickness*2;
        if(iw>0&&ih>0)fill(c,x+thickness,y+thickness,iw,ih,Math.max(0,radius-thickness),inner);
    }
}
