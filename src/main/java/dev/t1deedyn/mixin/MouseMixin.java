package dev.t1deedyn.mixin;

import dev.t1deedyn.Modules;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Inject(method="onMouseScroll",at=@At("HEAD"),cancellable=true)
    private void blackclover$zoomWheel(long window,double horizontal,double vertical,CallbackInfo ci){
        if(!Modules.ZOOM.isEnabled())return;
        if(Modules.ZOOM.onWheel(vertical))ci.cancel();
    }
}
