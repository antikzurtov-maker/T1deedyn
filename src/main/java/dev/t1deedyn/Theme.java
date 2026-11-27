package dev.t1deedyn;

/** Central, allocation-free palette for all Black Clover themes. */
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

    public enum Preset {
        CLOVER,OCEAN,ULTRAVIOLET,SUNSET,ROSE,RASPBERRY,GOLD,LIME,TURQUOISE,ICE,
        LAVENDER,BLOOD,MINT,COSMOS,MONOCHROME,AMBER,SAKURA,EMERALD,LAVA,RAINBOW
    }
    private static final int[] A={
        0xFF2BE36B,0xFF2EC4F1,0xFFB15CFF,0xFFFF7A3D,0xFFFF5FA2,
        0xFFE0345B,0xFFF2C14E,0xFFB6F23A,0xFF2EE6C9,0xFF9BDCFF,
        0xFFB8A1FF,0xFFD1252B,0xFF7CF0B0,0xFF7B5CFF,0xFFD0D4D8,
        0xFFFFB000,0xFFFFB7D5,0xFF00C781,0xFFFF4B1F,0xFF2BE36B
    };
    private static final int[] B={
        0xFF0FA84A,0xFF1E7FD8,0xFF6A3DE8,0xFFFF3D6E,0xFFC23CFF,
        0xFFA3123A,0xFFE08A1E,0xFF4CD137,0xFF1DA1F2,0xFFE0F4FF,
        0xFF8F7CFF,0xFF5A0E12,0xFF3DCCA0,0xFF00D1FF,0xFF8A9096,
        0xFFFF6B00,0xFFFF8FB8,0xFF007A52,0xFFFFB347,0xFF0FA84A
    };
    private static final int[] RAINBOW=new int[120];
    private static final Theme INSTANCE=new Theme(ACCENT);
    private static Preset selected=Preset.CLOVER;
    private static int customAccent,customAccent2;
    private static boolean tintBackground=true;
    public int accent,accentBright,accentDim,panelTint,contrastText;
    private int source;
    static {
        for(int i=0;i<RAINBOW.length;i++){
            float h=i/(float)RAINBOW.length*6.0f;
            int sector=(int)h;float f=h-sector;
            float q=1.0f-f;
            int r=0,g=0,b=0;
            switch(sector%6){
                case 0->{r=255;g=(int)(f*255);}
                case 1->{r=(int)(q*255);g=255;}
                case 2->{g=255;b=(int)(f*255);}
                case 3->{g=(int)(q*255);b=255;}
                case 4->{r=(int)(f*255);b=255;}
                default->{r=255;b=(int)(q*255);}
            }
            RAINBOW[i]=0xFF000000|(r<<16)|(g<<8)|b;
        }
    }
    private Theme(int color){update(color);}
    private void update(int color){
        source=color|0xFF000000;
        int r=(source>>>16)&255,g=(source>>>8)&255,b=source&255;
        accent=source;accentBright=rgb(Math.min(255,(int)(r*1.18f)),Math.min(255,(int)(g*1.18f)),Math.min(255,(int)(b*1.18f)));
        accentDim=0x40000000|(source&0xFFFFFF);panelTint=0x1F000000|(source&0xFFFFFF);
        contrastText=(0.2126*r+0.7152*g+0.0722*b)>148?0xFF17131F:0xFFFFFFFF;
    }
    private static int rgb(int r,int g,int b){return 0xFF000000|(r<<16)|(g<<8)|b;}
    private static int mix(int a,int b,int amount){
        int inv=255-amount;
        int r=((((a>>>16)&255)*inv+((b>>>16)&255)*amount)/255);
        int g=((((a>>>8)&255)*inv+((b>>>8)&255)*amount)/255);
        int bl=(((a&255)*inv+(b&255)*amount)/255);
        return rgb(r,g,bl);
    }
    public static Theme get(int color){if(INSTANCE.source!=(color|0xFF000000))INSTANCE.update(color);return INSTANCE;}
    public static void select(Preset preset){selected=preset==null?Preset.CLOVER:preset;customAccent=0;customAccent2=0;}
    public static Preset selected(){return selected;}
    public static void setCustomAccent(int color){customAccent=0xFF000000|(color&0xFFFFFF);}
    public static void setCustomAccent2(int color){customAccent2=0xFF000000|(color&0xFFFFFF);}
    public static void setTintBackground(boolean enabled){tintBackground=enabled;}
    public static boolean tintBackground(){return tintBackground;}
    public static int accentColor(){
        if(customAccent!=0)return customAccent;
        if(selected==Preset.RAINBOW)return RAINBOW[(int)((System.currentTimeMillis()/100L)%RAINBOW.length)];
        return A[selected.ordinal()];
    }
    public static int accent2Color(){
        if(customAccent2!=0)return customAccent2;
        if(selected==Preset.RAINBOW)return RAINBOW[(int)((System.currentTimeMillis()/100L+28)%RAINBOW.length)];
        return B[selected.ordinal()];
    }
    public static int brightAccentColor(){
        int c=accentColor(),r=Math.min(255,(int)(((c>>>16)&255)*1.18f));
        int g=Math.min(255,(int)(((c>>>8)&255)*1.18f)),b=Math.min(255,(int)((c&255)*1.18f));
        return rgb(r,g,b);
    }
    public static int backgroundColor(){return tintBackground?mix(0xFF06080A,accentColor(),13):0xFF06080A;}
    public static int panelColor(){int bg=backgroundColor();return tintBackground?mix(bg,accentColor(),10):PANEL;}
    public static int cardColor(){int bg=backgroundColor();return tintBackground?mix(bg,accentColor(),18):CARD;}
    public static int borderColor(){int c=accentColor();return 0x47000000|(c&0xFFFFFF);}
    public static String displayName(Preset p){
        return switch(p){
            case CLOVER->"Клевер";case OCEAN->"Океан";case ULTRAVIOLET->"Ультрафиолет";case SUNSET->"Закат";
            case ROSE->"Роза";case RASPBERRY->"Малина";case GOLD->"Золото";case LIME->"Лайм";
            case TURQUOISE->"Бирюза";case ICE->"Лёд";case LAVENDER->"Лаванда";case BLOOD->"Кровь";
            case MINT->"Мята";case COSMOS->"Космос";case MONOCHROME->"Монохром";case AMBER->"Янтарь";
            case SAKURA->"Сакура";case EMERALD->"Изумруд";case LAVA->"Лава";case RAINBOW->"Радуга";
        };
    }
}
