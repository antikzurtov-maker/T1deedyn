package dev.t1deedyn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

public final class T1deedynClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("T1deedyn");
    private static KeyBinding openMenu;
    private static KeyBinding toggleAutoBerry;

    public static void setAutoBerryKey(int keyCode) {
        if (toggleAutoBerry != null) {
            toggleAutoBerry.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
            KeyBinding.updateKeysByCode();
        }
    }

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.t1deedyn.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X,
                "category.t1deedyn.main"));
        toggleAutoBerry = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.t1deedyn.auto_berry", InputUtil.Type.KEYSYM, ModConfig.autoBerryToggleKey < 0 ? GLFW.GLFW_KEY_UNKNOWN : ModConfig.autoBerryToggleKey,
                "category.t1deedyn.main"));
        AutoBerry.registerTick();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleAutoBerry.wasPressed()) AutoBerry.toggle();
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new T1deedynScreen());
                else if (client.currentScreen instanceof T1deedynScreen) client.setScreen(null);
            }
        });

        // Fabric API 0.102.0+1.21: use the legacy callback only.
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> VisualHud.render(MinecraftClient.getInstance(), drawContext));
    }
}
