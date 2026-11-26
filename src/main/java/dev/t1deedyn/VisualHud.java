package dev.t1deedyn;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

final class VisualHud {
    private static boolean failed;

    private VisualHud() {}

    static void render(MinecraftClient client, DrawContext context) {
        if (failed || client == null || client.options == null || client.currentScreen != null) return;
        if (!ModConfig.showFps && !ModConfig.showPing && !ModConfig.showHudCoordinates && !ModConfig.berryHighlightDebug) return;
        try {
            ClientPlayerEntity player = client.player;
            int y = 6;
            if (ModConfig.showFps) {
                context.drawText(client.textRenderer, Text.literal("FPS: " + client.getCurrentFps()), 6, y, 0xFFEDE7FF, true);
                y += 11;
            }
            if (ModConfig.showPing && player != null) {
                ClientPlayNetworkHandler network = client.getNetworkHandler();
                if (network != null) {
                    PlayerListEntry entry = network.getPlayerListEntry(player.getUuid());
                    if (entry != null) {
                        context.drawText(client.textRenderer, Text.literal("Ping: " + entry.getLatency() + " ms"), 6, y, 0xFFEDE7FF, true);
                        y += 11;
                    }
                }
            }
            if (ModConfig.berryHighlightDebug && player != null && client.world != null) {
                context.drawText(client.textRenderer, Text.literal("Ягоды: " + BerryHighlightRenderer.count()), 6, y, 0xFF9FE8C2, true);
                y += 11;
            }
            if (ModConfig.autoBerryCountHud && player != null && client.world != null) {
                context.drawText(client.textRenderer, Text.literal("Собрано: " + AutoBerry.getCollected()), 6, y, 0xFF9FE8C2, true);
                y += 11;
            }
            if (ModConfig.showHudCoordinates && player != null && client.world != null) {
                String coords = String.format(java.util.Locale.ROOT, "XYZ: %d %d %d",
                        (int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()));
                context.drawText(client.textRenderer, Text.literal(coords), 6, y, 0xFFEDE7FF, true);
            }
        } catch (RuntimeException exception) {
            failed = true;
            T1deedynClient.LOGGER.warn("HUD module disabled after render error: {}", exception.toString());
        }
    }
}
