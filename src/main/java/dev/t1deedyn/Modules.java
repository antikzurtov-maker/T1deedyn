package dev.t1deedyn;

/** Stable accessors referenced by mixins. */
public final class Modules {
    public static final FullbrightModule FULLBRIGHT=new FullbrightModule();
    public static final ZoomModule ZOOM=new ZoomModule();
    public static final CustomSkyModule CUSTOM_SKY=new CustomSkyModule();
    private Modules(){}
    public static void initialize(){
        ModuleManager.register(FULLBRIGHT);
        ModuleManager.register(ZOOM);
        ModuleManager.register(CUSTOM_SKY);
        ZOOM.initializeKey();
    }
}
