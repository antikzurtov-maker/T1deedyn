package dev.t1deedyn.mixin;

import dev.t1deedyn.Modules;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void blackclover$zoom(Camera camera,float tickDelta,boolean changingFov,CallbackInfoReturnable<Double> cir){
        if(!Modules.ZOOM.isEnabled())return;
        double factor=Modules.ZOOM.factor();
        if(factor<=1.001)return;
        cir.setReturnValue(cir.getReturnValue()/factor);
    }
}
