package dev.t1deedyn.mixin;

import dev.t1deedyn.EntityAnimationsRenderer;
import dev.t1deedyn.Modules;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
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
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void blackclover$animateHumanoid(LivingEntity entity,
                                              float limbAngle,
                                              float limbDistance,
                                              float animationProgress,
                                              float headYaw,
                                              float headPitch,
                                              CallbackInfo ci) {
        if (!Modules.ENTITY_ANIMATIONS.isEnabled() || entity.isFallFlying()) return;

        float walk = Math.min(1.0f, Math.max(0.0f, limbDistance));
        float amount = EntityAnimationsRenderer.intensity();
        boolean sneaking = entity.isInSneakingPose();
        boolean sprinting = entity.isSprinting();
        boolean swimming = entity.isInSwimmingPose();

        body.roll += EntityAnimationsRenderer.bodyRoll(animationProgress, walk, sneaking, sprinting);
        head.roll += EntityAnimationsRenderer.headRoll(animationProgress, walk);
        rightArm.roll += EntityAnimationsRenderer.armRoll(animationProgress, walk, true, sprinting);
        leftArm.roll += EntityAnimationsRenderer.armRoll(animationProgress, walk, false, sprinting);

        float idle = EntityAnimationsRenderer.idleSway(animationProgress);
        body.pitch += idle * (sneaking ? 0.35f : 1.0f);

        if (swimming) {
            float swimWave = (float) Math.sin(animationProgress * 0.22f) * amount;
            body.pitch -= 0.12f * amount;
            rightArm.pitch += swimWave * 0.10f;
            leftArm.pitch -= swimWave * 0.10f;
            rightLeg.pitch += swimWave * 0.08f;
            leftLeg.pitch -= swimWave * 0.08f;
        } else {
            float airborne = EntityAnimationsRenderer.jumpAmount(entity.getVelocity().y, entity.isOnGround());
            rightArm.pitch -= airborne * 0.12f * amount;
            leftArm.pitch -= airborne * 0.12f * amount;
            rightLeg.pitch -= airborne * 0.16f * amount;
            leftLeg.pitch -= airborne * 0.16f * amount;
        }

        float swing = EntityAnimationsRenderer.swingAmount(entity.handSwingProgress);
        if (swing > 0.001f) {
            float attack = swing * amount;
            body.yaw += attack * 0.07f;
            rightArm.roll += attack * 0.12f;
            leftArm.roll -= attack * 0.12f;
        }

        if (entity.isUsingItem()) {
            rightArm.roll += 0.025f * amount;
            leftArm.roll -= 0.025f * amount;
        }

        if (entity.hurtTime > 0) {
            float hurt = Math.min(1.0f, entity.hurtTime / 10.0f) * amount;
            body.roll += hurt * 0.045f;
            head.roll -= hurt * 0.025f;
        }
    }
}
