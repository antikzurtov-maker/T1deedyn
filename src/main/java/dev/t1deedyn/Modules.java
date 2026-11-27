package dev.t1deedyn;

/** Stable accessors referenced by mixins. */
public final class Modules {
    public static final FullbrightModule FULLBRIGHT=new FullbrightModule();
    public static final ZoomModule ZOOM=new ZoomModule();
    public static final CustomSkyModule CUSTOM_SKY=new CustomSkyModule();
    public static final EntityAnimationsModule ENTITY_ANIMATIONS=new EntityAnimationsModule();
    private Modules(){}
    public static void initialize(){
        ModuleManager.register(FULLBRIGHT);
        ModuleManager.register(ZOOM);
        ModuleManager.register(CUSTOM_SKY);
        ModuleManager.register(ENTITY_ANIMATIONS);
        ZOOM.initializeKey();
    }
}
