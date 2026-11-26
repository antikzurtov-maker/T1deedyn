package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Procedural clover glyph with cached horizontal pixel runs. */
public final class CloverMark {
    private static final int SIZE=64;
    private static final int[][] LEFT=new int[SIZE][32], RIGHT=new int[SIZE][32];
    private static final int[] COUNTS=new int[SIZE];
    static {
        for(int y=0;y<SIZE;y++){
            int run=-1,count=0;
            for(int x=0;x<=SIZE;x++){
                boolean on=x<SIZE&&inside(x+0.5,y+0.5);
                if(on&&run<0)run=x;
                if(!on&&run>=0){LEFT[y][count]=run;RIGHT[y][count]=x;count++;run=-1;}
            }
            COUNTS[y]=count;
        }
    }
    private CloverMark(){}
    private static boolean inside(double x,double y){
        // Four round heart-lobes with a shared center and a short stem.
        double[][] centers={{32,18},{46,32},{32,46},{18,32}};
        for(int i=0;i<4;i++){double dx=x-centers[i][0],dy=y-centers[i][1];if(dx*dx+dy*dy<=11.5*11.5)return true;}
        return (x>=28&&x<=36&&y>=28&&y<=48)||(x>=30&&x<=34&&y>=45&&y<=60);
    }
    public static void draw(DrawContext c,int x,int y,int size,int color){
        if(size<=0)return;
        for(int row=0;row<size;row++){
            int sy=row*SIZE/size;
            for(int i=0;i<COUNTS[sy];i++){
                int l=LEFT[sy][i]*size/SIZE;
                int r=(RIGHT[sy][i]*size+SIZE-1)/SIZE;
                if(r>l)c.fill(x+l,y+row,x+r,y+row+1,color);
            }
        }
    }
}
