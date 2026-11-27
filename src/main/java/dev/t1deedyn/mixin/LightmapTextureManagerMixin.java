package dev.t1deedyn.mixin;

import dev.t1deedyn.Modules;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Inject(method="getBrightness",at=@At("RETURN"),cancellable=true)
    private static void blackclover$fullbright(DimensionType type,int lightLevel,CallbackInfoReturnable<Float> cir){
        if(!Modules.FULLBRIGHT.isEnabled())return;
        if(!Modules.FULLBRIGHT.shouldApply())return;
        float factor=Modules.FULLBRIGHT.factor();
        if(factor<=0.001f)return;
        cir.setReturnValue(cir.getReturnValue()+(1.0f-cir.getReturnValue())*factor);
    }
}
