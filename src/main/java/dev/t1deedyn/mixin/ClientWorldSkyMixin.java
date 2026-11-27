package dev.t1deedyn.mixin;

import dev.t1deedyn.CustomSkyRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldSkyMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void blackclover$customSkyColor(Vec3d cameraPos, float tickDelta,
                                           CallbackInfoReturnable<Vec3d> cir) {
        ClientWorld world = (ClientWorld) (Object) this;
        cir.setReturnValue(CustomSkyRenderer.skyColor(world, tickDelta, cir.getReturnValue()));
    }

    @Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true)
    private void blackclover$customCloudColor(float tickDelta,
                                              CallbackInfoReturnable<Vec3d> cir) {
        ClientWorld world = (ClientWorld) (Object) this;
        cir.setReturnValue(CustomSkyRenderer.cloudColor(world, tickDelta, cir.getReturnValue()));
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void blackclover$customStarBrightness(float tickDelta,
                                                   CallbackInfoReturnable<Float> cir) {
        ClientWorld world = (ClientWorld) (Object) this;
        cir.setReturnValue(CustomSkyRenderer.starBrightness(world, cir.getReturnValue()));
    }
}
