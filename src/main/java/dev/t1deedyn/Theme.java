package dev.t1deedyn;

/** Central ARGB palette for the Black Clover interface. */
public final class Theme {
    public static final int BG = 0xEB070A08;
    public static final int PANEL = 0xFF0D1410;
    public static final int CARD = 0xFF111A14;
    public static final int CARD_HOVER = 0xFF16231B;
    public static final int BORDER = 0xFF1E3A28;
    public static final int ACCENT = 0xFF2BE36B;
    public static final int ACCENT_BRIGHT = 0xFF6BFF9A;
    public static final int TEXT = 0xFFE8F3EC;
    public static final int TEXT_DIM = 0xFF8FA699;
    public static final int TOGGLE_OFF = 0xFF2A2F2C;
    public static final int DANGER = 0xFFE04B4B;
    public enum Preset { CLOVER, OCEAN, NEON, SUNSET, MONO }
    private static final Theme INSTANCE = new Theme(ACCENT);
    private static Preset selected = Preset.CLOVER;
    private static int customAccent;
    public int accent, accentBright, accentDim, panelTint, contrastText;
    private int source;
    private Theme(int color) { update(color); }
    private void update(int color) {
        source = color | 0xFF000000;
        int r=(source>>>16)&255, g=(source>>>8)&255, b=source&255;
        accent=source;
        accentBright=rgb(Math.min(255,(int)(r*1.18f)),Math.min(255,(int)(g*1.18f)),Math.min(255,(int)(b*1.18f)));
        accentDim=(0x40000000)|(source&0x00FFFFFF);
        panelTint=(0x1F000000)|(source&0x00FFFFFF);
        double lum=(0.2126*r+0.7152*g+0.0722*b)/255.0;
        contrastText=lum>0.58?0xFF17131F:0xFFFFFFFF;
    }
    private static int rgb(int r,int g,int b){return 0xFF000000|(r<<16)|(g<<8)|b;}
    public static Theme get(int color){if(INSTANCE.source!=(color|0xFF000000))INSTANCE.update(color);return INSTANCE;}
    public static void select(Preset preset){selected=preset==null?Preset.CLOVER:preset;customAccent=0;}
    public static Preset selected(){return selected;}
    public static void setCustomAccent(int color){customAccent=0xFF000000|(color&0x00FFFFFF);}
    public static int accentColor(){
        if(customAccent!=0)return customAccent;
        return switch(selected){
            case CLOVER -> ACCENT;
            case OCEAN -> 0xFF36C9E8;
            case NEON -> 0xFFB56BFF;
            case SUNSET -> 0xFFFF984A;
            case MONO -> 0xFFB8C0BA;
        };
    }
    public static int brightAccentColor(){
        if(selected==Preset.CLOVER && customAccent==0)return ACCENT_BRIGHT;
        int c=accentColor(),r=Math.min(255,(int)(((c>>>16)&255)*1.18f));
        int g=Math.min(255,(int)(((c>>>8)&255)*1.18f)),b=Math.min(255,(int)((c&255)*1.18f));
        return rgb(r,g,b);
    }
}
