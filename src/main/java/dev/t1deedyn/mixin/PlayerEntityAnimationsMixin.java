package dev.t1deedyn.mixin;

import dev.t1deedyn.EntityAnimationsRenderer;
import dev.t1deedyn.Modules;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class PlayerEntityAnimationsMixin {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void blackclover$animatePlayer(LivingEntity entity,
                                           float limbAngle,
                                           float limbDistance,
                                           float animationProgress,
                                           float headYaw,
                                           float headPitch,
                                           CallbackInfo ci) {
        if (!Modules.ENTITY_ANIMATIONS.isEnabled()
                || !(entity instanceof PlayerEntity)
                || entity.isFallFlying()
                || entity.isSwimming()
                || entity.isUsingItem()) {
            return;
        }

        float walk = Math.min(1.0f, Math.max(0.0f, limbDistance));
        boolean sneaking = entity.isInSneakingPose();
        body.roll += EntityAnimationsRenderer.bodyRoll(animationProgress, walk, sneaking);
        head.roll += EntityAnimationsRenderer.headRoll(animationProgress, walk);
        rightArm.roll += EntityAnimationsRenderer.armRoll(animationProgress, walk, true);
        leftArm.roll += EntityAnimationsRenderer.armRoll(animationProgress, walk, false);

        float idle = EntityAnimationsRenderer.idleSway(animationProgress);
        body.pitch += idle * (sneaking ? 0.35f : 1.0f);
    }
}
