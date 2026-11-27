package dev.t1deedyn;

import net.minecraft.client.gui.DrawContext;

/** Shared 40x20 animated toggle. All motion is time-based; per-widget state is preallocated. */
public final class Toggle {
    private static final int CAPACITY=128;
    private static final float[] PROGRESS=new float[CAPACITY];
    private static final long[] LAST=new long[CAPACITY];
    private static final boolean[] INITIALIZED=new boolean[CAPACITY];
    private Toggle(){}
    public static void draw(DrawContext c,int id,int x,int y,boolean enabled,int accent,int accent2,
                            boolean hovered,boolean pressed,boolean available){
        int slot=Math.max(0,Math.min(CAPACITY-1,id));
        long now=System.nanoTime();
        if(!INITIALIZED[slot]){PROGRESS[slot]=enabled?1f:0f;LAST[slot]=now;INITIALIZED[slot]=true;}
        float p=PROGRESS[slot];
        float dt=Math.min(0.05f,Math.max(0f,(now-LAST[slot])/1_000_000_000f));
        LAST[slot]=now;
        float target=enabled?1f:0f;
        float step=Math.min(1f,dt/0.180f);
        float eased=1f-(float)Math.pow(1f-step,3.0);
        p+=(target-p)*eased;
        if(Math.abs(target-p)<0.001f)p=target;
        PROGRESS[slot]=p;
        int alpha=available?255:102;
        int off=mix(0xFF232826,accent,15);
        int track=mix(off,accent,(int)(p*255));
        if(hovered)track=lighten(track,0.08f);
        int tx=x,ty=y;
        if(p>0.01f){
            int glowAlpha=(int)(p*89);
            RoundedRect.fill(c,tx-4,ty-4,48,28,14,(glowAlpha<<24)|(accent&0xFFFFFF));
        }
        RoundedRect.fill(c,tx,ty,40,20,10,withAlpha(track,alpha));
        RoundedRect.border(c,tx,ty,40,20,10,1,withAlpha(mix(track,0xFF000000,64),alpha),withAlpha(track,alpha));
        if(p>0.01f){
            int left=tx+2,right=tx+38;
            for(int row=0;row<8;row++){
                int a=(int)(p*31*(1f-row/8f));
                c.fill(left,ty+1+row,right,ty+2+row,(a<<24)|0xFFFFFF);
            }
        }
        int knobW=pressed?18:(int)Math.round(14+4*Math.sin(p*Math.PI));
        if(pressed)knobW=18;
        int knobX=tx+3+(int)Math.round(p*20)+(14-knobW)/2;
        int knobY=ty+3;
        RoundedRect.fill(c,knobX,knobY+1,knobW,14,7,0x59000000);
        RoundedRect.fill(c,knobX,knobY,knobW,14,7,withAlpha(enabled?0xFFFFFFFF:0xFFC9D1CC,alpha));
        if(enabled){
            c.fill(knobX+knobW/2-3,knobY+7,knobX+knobW/2-1,knobY+9,withAlpha(accent,alpha));
            c.fill(knobX+knobW/2-1,knobY+5,knobX+knobW/2+1,knobY+8,withAlpha(accent,alpha));
            c.fill(knobX+knobW/2+1,knobY+4,knobX+knobW/2+3,knobY+6,withAlpha(accent,alpha));
        }else c.fill(knobX+5,knobY+6,knobX+9,knobY+8,withAlpha(0xFF89918C,alpha));
    }
    private static int mix(int a,int b,int amount){
        int inv=255-amount;
        int r=(((a>>>16)&255)*inv+((b>>>16)&255)*amount)/255;
        int g=(((a>>>8)&255)*inv+((b>>>8)&255)*amount)/255;
        int bl=((a&255)*inv+(b&255)*amount)/255;
        return 0xFF000000|(r<<16)|(g<<8)|bl;
    }
    private static int lighten(int c,float amount){
        int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
        return 0xFF000000|(Math.min(255,(int)(r+(255-r)*amount))<<16)
                |(Math.min(255,(int)(g+(255-g)*amount))<<8)|Math.min(255,(int)(b+(255-b)*amount));
    }
    private static int withAlpha(int color,int alpha){
        return(color&0x00FFFFFF)|((((color>>>24)&255)*alpha/255)<<24);
    }
}
