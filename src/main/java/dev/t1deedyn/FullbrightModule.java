package dev.t1deedyn;

import java.util.List;

/** Client-only lightmap boost. It never adds a potion effect or changes server state. */
public final class FullbrightModule extends Module {
    private long transitionStart;
    private float current;
    public FullbrightModule(){super("fullbright","Полный свет","Осветление клиентской карты освещения",0,0,
        List.of(new Setting("mode","Режим",0,1,0),new Setting("brightness","Яркость",0,100,100),
               new Setting("smooth","Плавное включение",0,1,1),new Setting("skipDimensions","Не трогать Незер и Энд",0,1,0)));}
    @Override public void onEnable(){transitionStart=System.nanoTime();}
    @Override public void onDisable(){transitionStart=System.nanoTime();current=0f;}
    public float factor(){
        if(!isEnabled())return 0f;
        if(!ModConfig.fullbrightSmooth)return ModConfig.fullbrightBrightness/100f;
        float t=Math.min(1f,(System.nanoTime()-transitionStart)/300_000_000f);
        return t*(ModConfig.fullbrightBrightness/100f);
    }
    public boolean shouldApply(){
        if(!isEnabled())return false;
        if(ModConfig.fullbrightSkipDimensions){
            var client=net.minecraft.client.MinecraftClient.getInstance();
            if(client.world!=null){
                String path=client.world.getRegistryKey().getValue().getPath();
                if("the_nether".equals(path)||"the_end".equals(path))return false;
            }
        }
        return true;
    }
}
