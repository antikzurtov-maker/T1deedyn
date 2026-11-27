package dev.t1deedyn;

import java.util.List;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Client-side animated FOV zoom. Default control is toggle mode for touch launchers. */
public final class ZoomModule extends Module {
    private KeyBinding key;
    private boolean toggled;
    private boolean keyWasDown;
    private double current=1.0,target=1.0;
    private long lastNanos=System.nanoTime();
    public ZoomModule(){super("zoom","Зум","Плавное приближение камеры",0,1,
        List.of(new Setting("mode","Режим",0,1,1),new Setting("default","Кратность",1.5,10,4),
               new Setting("speed","Скорость",1,20,10),new Setting("wheel","Колесо меняет кратность",0,1,1),
               new Setting("sensitivity","Снижать чувствительность",0,1,1),
               new Setting("smoothCamera","Плавная камера при зуме",0,1,0)));}
    public void initializeKey(){
        key=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.t1deedyn.zoom",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_C,"category.t1deedyn.main"));
    }
    @Override public void onEnable(){target=clamp(ModConfig.zoomDefault,1.5,10.0);toggled=false;lastNanos=System.nanoTime();}
    @Override public void onDisable(){toggled=false;target=1.0;}
    @Override public void onTick(){
        if(key!=null){
            boolean down=key.isPressed();
            if(ModConfig.zoomToggleMode){if(key.wasPressed())toggled=!toggled;}
            else toggled=down;
            keyWasDown=down;
        }
        MinecraftClient client=MinecraftClient.getInstance();
        if(client.player==null||client.currentScreen!=null||client.player.isDead()){
            toggled=false;target=1.0;
        }else target=toggled?clamp(ModConfig.zoomDefault,1.5,10.0):1.0;
        long now=System.nanoTime();
        double dt=Math.min(0.1,Math.max(0.0,(now-lastNanos)/1_000_000_000.0));
        lastNanos=now;
        if(!isEnabled()){current=1.0;return;}
        double speed=clamp(ModConfig.zoomSpeed,1,20);
        current+=(target-current)*(1.0-Math.exp(-dt*speed*3.0));
        if(Math.abs(current-1.0)<0.001&&!toggled)current=1.0;
    }
    public boolean active(){return isEnabled()&&(toggled||current>1.001);}
    public double factor(){return current;}
    public boolean onWheel(double amount){
        if(!isEnabled()||!active()||!ModConfig.zoomWheel)return false;
        ModConfig.zoomDefault=clamp(ModConfig.zoomDefault+(amount>0?0.5:-0.5),1.5,10.0);
        target=ModConfig.zoomDefault;ModConfig.save();return true;
    }
    public void reset(){toggled=false;target=1.0;}
    private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
}
