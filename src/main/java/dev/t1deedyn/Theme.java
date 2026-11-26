package dev.t1deedyn;

/** Cached theme palette. Recomputed only when the accent changes. */
public final class Theme {
    public int accent, accentBright, accentDim, panelTint, contrastText;
    private int source;
    private Theme(int color) { update(color); }
    private void update(int color) {
        source=color|0xFF000000;
        int r=(source>>16)&255,g=(source>>8)&255,b=source&255;
        accent=source;
        accentBright=rgb(Math.min(255,(int)(r*1.15)),Math.min(255,(int)(g*1.15)),Math.min(255,(int)(b*1.15)));
        accentDim=withAlpha(source,45);
        panelTint=withAlpha(source,31);
        double lum=(0.2126*r+0.7152*g+0.0722*b)/255.0;
        contrastText=lum>0.58?0xFF17131F:0xFFFFFFFF;
    }
    private static int rgb(int r,int g,int b){return 0xFF000000|(r<<16)|(g<<8)|b;}
    private static int withAlpha(int c,int a){return (a<<24)|(c&0xFFFFFF);}
    private static final Theme INSTANCE=new Theme(0xFF9B70FF);
    public static Theme get(int color){if(INSTANCE.source!=(color|0xFF000000))INSTANCE.update(color);return INSTANCE;}
}
