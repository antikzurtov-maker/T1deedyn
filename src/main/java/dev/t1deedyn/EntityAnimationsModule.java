package dev.t1deedyn;

import java.util.List;

/** Client-side player model animation module. */
public final class EntityAnimationsModule extends Module {
    public EntityAnimationsModule() {
        super("entity_animations", "EntityAnimations",
                "Плавные дополнительные анимации моделей игроков", 3, 5, List.of(
                new Setting("mode", "Стиль анимации", 0, 2, 0),
                new Setting("intensity", "Интенсивность", 0.0, 1.0, 0.65)
        ));
    }

    public void cycleMode() {
        ModConfig.entityAnimationMode = (ModConfig.entityAnimationMode + 1) % 3;
        ModConfig.save();
    }
}
