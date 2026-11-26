package dev.t1deedyn;

/** Preallocated slider hitbox geometry. Coordinates are relative to the content viewport. */
final class SliderHitboxes {
    static final class Rect {
        int x, y, w, h;
        void set(int x, int y, int w, int h) { this.x=x; this.y=y; this.w=w; this.h=h; }
        boolean contains(int px, int py) { return px>=x && px<x+w && py>=y && py<y+h; }
    }
    private final Rect[] rects = new Rect[6];
    SliderHitboxes() { for (int i=0;i<rects.length;i++) rects[i]=new Rect(); }
    Rect get(int id) { return rects[id]; }
    int find(int x, int y) {
        int best=-1, bestDistance=Integer.MAX_VALUE;
        for (int i=0;i<rects.length;i++) {
            Rect r=rects[i];
            if (r.w<=0 || r.h<=0) continue;
            int top=r.y, bottom=r.y+r.h;
            if (x<r.x || x>=r.x+r.w || y<top || y>=bottom) continue;
            int center=r.y+r.h/2, distance=Math.abs(y-center);
            if (distance<bestDistance) { best=i; bestDistance=distance; }
        }
        return best;
    }
}
