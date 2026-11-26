package dev.t1deedyn;

import java.util.Arrays;
import net.minecraft.client.gui.DrawContext;

/** Reusable integer-pixel scrolling model for a menu content viewport. */
public final class ScrollPanel {
    private final double[] targets = new double[7];
    private final double[] currents = new double[7];
    private int tab = -1, viewX, viewY, viewW, viewH, contentH;
    private boolean draggingThumb, draggingContent, moved;
    private double pressY, pressScroll, thumbGrab;
    private long lastActivity = System.nanoTime();

    public void layout(int tab, int x, int y, int width, int height, int contentHeight) {
        this.tab = Math.max(0, Math.min(6, tab));
        viewX=x; viewY=y; viewW=Math.max(0,width); viewH=Math.max(0,height);
        contentH=Math.max(0,contentHeight);
        clampTarget();
    }
    public int offset() { return (int)Math.round(currents[tab]); }
    public int target() { return (int)Math.round(targets[tab]); }
    public int maxScroll() { return Math.max(0,contentH-viewH); }
    public boolean needed() { return contentH>viewH; }
    public int contentY(int y) { return y-offset(); }
    public void tick(double dt) {
        double d=targets[tab]-currents[tab];
        if(Math.abs(d)<0.5) currents[tab]=targets[tab];
        else currents[tab]+=d*(1.0-Math.exp(-Math.max(0,dt)*16.0));
    }
    private void clampTarget() { targets[tab]=Math.max(0,Math.min(maxScroll(),targets[tab])); }
    public void scrollBy(double amount) { targets[tab]+=amount; clampTarget(); lastActivity=System.nanoTime(); }
    public boolean wheel(double mx,double my,double amount) {
        if(!needed() || !inside(mx,my)) return false;
        scrollBy(-amount*28.0); return true;
    }
    private boolean inside(double mx,double my) { return mx>=viewX && mx<=viewX+viewW && my>=viewY && my<=viewY+viewH; }
    private int thumbH() { return Math.max(28,(int)((long)viewH*viewH/Math.max(1,contentH))); }
    private int thumbY() { int track=Math.max(1,viewH-thumbH()); return viewY+(int)Math.round(track*(currents[tab]/Math.max(1,maxScroll()))); }
    public boolean mouseDown(double mx,double my) {
        if(!needed() || !inside(mx,my)) return false;
        int tx=viewX+viewW-11, th=thumbH(), ty=thumbY();
        if(mx>=tx-6 && mx<=tx+8) {
            if(my>=ty && my<=ty+th) { draggingThumb=true; thumbGrab=my-ty; }
            else { scrollBy(my<ty?-viewH:viewH); }
            return true;
        }
        draggingContent=true; moved=false; pressY=my; pressScroll=targets[tab]; return false;
    }
    public boolean mouseMove(double mx,double my) {
        if(draggingThumb) {
            int track=Math.max(1,viewH-thumbH());
            targets[tab]=Math.max(0,Math.min(maxScroll(),(my-viewY-thumbGrab)/track*maxScroll()));
            currents[tab]=targets[tab]; lastActivity=System.nanoTime(); return true;
        }
        if(draggingContent) {
            double delta=pressY-my;
            if(Math.abs(delta)>4) moved=true;
            if(moved) { targets[tab]=Math.max(0,Math.min(maxScroll(),pressScroll+delta)); currents[tab]=targets[tab]; lastActivity=System.nanoTime(); return true; }
        }
        return false;
    }
    public boolean mouseUp() { boolean was=draggingThumb||draggingContent; draggingThumb=false; draggingContent=false; return was; }
    public boolean consumedClick() { return moved; }
    public boolean key(int key) {
        switch(key) {
            case 265 -> scrollBy(-28); case 264 -> scrollBy(28);
            case 266 -> scrollBy(-viewH); case 267 -> scrollBy(viewH);
            case 268 -> { targets[tab]=0; lastActivity=System.nanoTime(); }
            case 269 -> { targets[tab]=maxScroll(); lastActivity=System.nanoTime(); }
            default -> { return false; }
        }
        clampTarget(); return true;
    }
    public void renderBar(DrawContext c,int accent) {
        if(!needed()) return;
        int x=viewX+viewW-5, th=thumbH(), ty=thumbY();
        c.fill(x,viewY,x+3,viewY+viewH,0x14FFFFFF);
        int alpha=System.nanoTime()-lastActivity<1_200_000_000L?217:64;
        int color=(alpha<<24)|(accent&0x00FFFFFF);
        c.fill(x,ty,x+3,ty+th,color);
    }
}
