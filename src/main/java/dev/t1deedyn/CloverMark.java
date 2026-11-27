package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Four individually shaded heart leaves, rasterized once with 4x supersampling. */
public final class CloverMark {
    private static final int SIZE=128, SS=4, MAX_RUNS=64, LEVELS=8;
    private static final short[][] START=new short[SIZE][MAX_RUNS],END=new short[SIZE][MAX_RUNS];
    private static final byte[][] SHADE=new byte[SIZE][MAX_RUNS];
    private static final byte[] COUNT=new byte[SIZE];
    static {
        final double cx=SIZE*0.5,cy=SIZE*0.5,r=60.0,scale=0.205*r,gap=0.03*r;
        for(int y=0;y<SIZE;y++){
            int run=-1,runShade=-1,count=0;
            for(int x=0;x<=SIZE;x++){
                int shade=0;
                if(x<SIZE){
                    int sum=0;
                    for(int sy=0;sy<SS;sy++)for(int sx=0;sx<SS;sx++){
                        double px=x+(sx+0.5)/SS-cx,py=cy-(y+(sy+0.5)/SS);
                        sum+=sample(px,py,scale,gap,r);
                    }
                    shade=sum/(SS*SS);
                }
                int level=shade==0?0:1+Math.min(LEVELS-1,(shade*(LEVELS-1))/255);
                if(level>0&&run<0){run=x;runShade=level;}
                if((level==0||level!=runShade)&&run>=0){
                    if(count<MAX_RUNS){START[y][count]=(short)run;END[y][count]=(short)x;SHADE[y][count]=(byte)runShade;count++;}
                    run=level>0?x:-1;runShade=level;
                }
            }
            COUNT[y]=(byte)count;
        }
    }
    private CloverMark(){}
    private static int sample(double x,double y,double scale,double gap,double radius){
        int result=0;
        // Draw in fixed order so the later leaf's dark edge creates a clean seam.
        for(int i=0;i<4;i++){
            double a=i*Math.PI*0.5,co=Math.cos(a),si=Math.sin(a);
            double lx=x*co+y*si,ly=-x*si+y*co;
            double u=lx/scale,v=(ly-gap)/scale-1.0;
            if(u < -1.14||u > 1.14||v < -1.0||v > 1.25)continue;
            double q=u*u+v*v-1.0;
            double f=q*q*q-u*u*v*v*v;
            if(f>0.0)continue;
            double lobes=Math.max(0.0,1.0-Math.abs(Math.abs(u)-0.60)/0.62);
            double outer=Math.max(0.0,Math.min(1.0,(v+1.0)/2.25));
            int shade=(int)(132+68*outer+28*lobes);
            if(f>-0.06)shade=62;
            result=shade;
        }
        // Curved stem, cubic Bezier from the center down and to the right.
        double best=Double.MAX_VALUE;
        double px0=0,py0=0;
        for(int i=1;i<=16;i++){
            double t=i/16.0,mt=1.0-t;
            double bx=mt*mt*mt*0.0+3*mt*mt*t*2.0+3*mt*t*t*15.0+t*t*t*0.45*radius;
            double by=mt*mt*mt*0.0+3*mt*mt*t*12.0+3*mt*t*t*34.0+t*t*t*0.45*radius;
            double dx=x-bx,dy=y-by,d=dx*dx+dy*dy;
            if(d<best){best=d;px0=bx;py0=by;}
        }
        if(y>=-1&&best<Math.pow(0.03*radius,2))result=Math.max(result,105);
        return result;
    }
    private static int tint(int color,int shade){
        int r=(color>>>16)&255,g=(color>>>8)&255,b=color&255;
        return 0xFF000000|((r*shade/255)<<16)|((g*shade/255)<<8)|(b*shade/255);
    }
    public static void draw(DrawContext context,int x,int y,int size,int color){
        if(size<=0)return;
        for(int row=0;row<size;row++){
            int sy=row*SIZE/size,count=COUNT[sy]&255;
            int dy=y+row;
            for(int i=0;i<count;i++){
                int left=(START[sy][i]*size)/SIZE;
                int right=(END[sy][i]*size+SIZE-1)/SIZE;
                if(right>left){
                    int level=SHADE[sy][i]&255;
                    int shade=level*255/LEVELS;
                    context.fill(x+left,dy,x+right,dy+1,tint(color,shade));
                }
            }
        }
    }
}
