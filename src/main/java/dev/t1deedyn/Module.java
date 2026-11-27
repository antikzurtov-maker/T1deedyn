package dev.t1deedyn;

import java.util.List;
import net.minecraft.client.gui.DrawContext;

/**
 * Module template: id/name/description/category/iconId/settings/enabled.
 * Implement onEnable/onDisable for transitions, onTick for expensive work,
 * onRender for drawing only, and buildPreview for optional settings previews.
 * Register a module with one ModuleManager.register(new YourModule()) call.
 */
public abstract class Module {
    public final String id,name,description;
    public final int category,iconId;
    public final List<Setting> settings;
    private boolean enabled;
    protected Module(String id,String name,String description,int category,int iconId,List<Setting> settings){
        this.id=id;this.name=name;this.description=description;this.category=category;this.iconId=iconId;this.settings=settings;
    }
    public final boolean isEnabled(){return enabled;}
    public final void setEnabled(boolean value){
        if(enabled==value)return;
        enabled=value;
        try{if(value)onEnable();else onDisable();}
        catch(Throwable error){enabled=false;T1deedynClient.LOGGER.error("Module {} disabled after error",id,error);}
    }
    public void onEnable(){}
    public void onDisable(){}
    public void onTick(){}
    public void onRender(DrawContext context,float tickDelta){}
    public void buildPreview(DrawContext context,int x,int y,int width,int height){}
}
