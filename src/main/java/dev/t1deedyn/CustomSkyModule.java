package dev.t1deedyn;

import java.util.List;

/** Client-only sky appearance module. Rendering is applied by narrow ClientWorld mixins. */
public final class CustomSkyModule extends Module {
    public CustomSkyModule() {
        super("custom_sky", "CustomSky", "Пользовательское оформление неба", 0, 0, List.of(
                new Setting("mode", "Режим неба", 0, 10, 0),
                new Setting("quality", "Качество", 0, 2, 1),
                new Setting("intensity", "Интенсивность", 0.0, 1.0, 0.85)
        ));
    }

    public void cycleMode() {
        ModConfig.skyMode = (ModConfig.skyMode + 1) % 11;
        ModConfig.save();
    }

    public void cycleQuality() {
        ModConfig.skyQuality = (ModConfig.skyQuality + 1) % 3;
        ModConfig.save();
    }
}
