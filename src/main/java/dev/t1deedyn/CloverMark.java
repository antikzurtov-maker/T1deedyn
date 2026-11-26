package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Cached integer raster of four inward-pointing heart leaves and a curved stem. */
public final class CloverMark {
    private static final int SIZE=128;
    private static final int[][] LEFT=new int[SIZE][8], RIGHT=new int[SIZE][8];
    private static final int[] COUNTS=new int[SIZE];
    static {
        final double cx=SIZE*0.5, cy=SIZE*0.5;
        for(int py=0;py<SIZE;py++){
            int run=-1,count=0;
            for(int px=0;px<=SIZE;px++){
                boolean on=px<SIZE&&inside(px+0.5,py+0.5,cx,cy);
                if(on&&run<0)run=px;
                if(!on&&run>=0){
                    if(count<LEFT[py].length){LEFT[py][count]=run;RIGHT[py][count]=px;count++;}
                    run=-1;
                }
            }
            COUNTS[py]=count;
        }
    }
    private CloverMark(){}
    private static boolean inside(double px,double py,double cx,double cy){
        double x=px-cx,y=py-cy;
        boolean leaf=false;
        // Heart equation, with the pointed end facing the clover's center.
        for(int i=0;i<4;i++){
            double a=i*Math.PI*0.5;
            double rx=x*Math.cos(a)+y*Math.sin(a);
            double ry=-x*Math.sin(a)+y*Math.cos(a);
            double lx=rx/27.0, ly=(ry+18.0)/27.0;
            double q=lx*lx+ly*ly-1.0;
            if(q*q*q-lx*lx*ly*ly<=0.0) leaf=true;
        }
        // Bezier-like narrow stem, running down and slightly right.
        double t=Math.max(0.0,Math.min(1.0,(y-8.0)/48.0));
        double bx=3.0*t*t, by=8.0+48.0*t;
        double dx=x-bx,dy=y-by;
        boolean stem=y>=8.0&&y<=57.0&&dx*dx+dy*dy<=2.4*2.4;
        return leaf||stem;
    }
    public static void draw(DrawContext context,int x,int y,int size,int color){
        if(size<=0)return;
        for(int row=0;row<size;row++){
            int sy=row*SIZE/size;
            for(int i=0;i<COUNTS[sy];i++){
                int left=LEFT[sy][i]*size/SIZE;
                int right=(RIGHT[sy][i]*size+SIZE-1)/SIZE;
                if(right>left)context.fill(x+left,y+row,x+right,y+row+1,color);
            }
        }
    }
}
