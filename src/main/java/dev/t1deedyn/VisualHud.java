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
        if (client.player == null || client.world == null) return;
        try {
            ClientPlayerEntity player = client.player;
            int y = 6;
            if (ModConfig.showFps) {
                context.drawText(client.textRenderer, Text.literal("FPS: " + client.getCurrentFps()), 8, y, Theme.brightAccentColor(), true);
                y += 12;
            }
            if (ModConfig.showPing && player != null) {
                ClientPlayNetworkHandler network = client.getNetworkHandler();
                if (network != null) {
                    PlayerListEntry entry = network.getPlayerListEntry(player.getUuid());
                    if (entry != null) {
                        context.drawText(client.textRenderer, Text.literal("Ping: " + entry.getLatency() + " ms"), 8, y, Theme.TEXT, true);
                        y += 12;
                    }
                }
            }
            if (ModConfig.berryHighlightDebug && player != null && client.world != null) {
                context.drawText(client.textRenderer, Text.literal("Ягоды: " + BerryHighlightRenderer.count()), 8, y, Theme.TEXT, true);
                y += 12;
            }
            if (ModConfig.autoBerryCountHud && player != null && client.world != null) {
                context.drawText(client.textRenderer, Text.literal("Собрано: " + AutoBerry.getCollected()), 8, y, Theme.TEXT, true);
                y += 12;
            }
            if (ModConfig.showHudCoordinates && player != null && client.world != null) {
                String coords = String.format(java.util.Locale.ROOT, "XYZ: %d %d %d",
                        (int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()));
                context.drawText(client.textRenderer, Text.literal(coords), 8, y, Theme.TEXT, true);
            }
            String brand = "ЧЁРНЫЙ КЛЕВЕР";
            String tagline = "Сила в тебе...";
            int brandWidth = client.textRenderer.getWidth(brand);
            int taglineWidth = client.textRenderer.getWidth(tagline);
            int right = client.getWindow().getScaledWidth() - 8;
            int brandX = right - brandWidth;
            context.fill(brandX - 8, 5, right + 5, 29, 0x66202C24);
            CloverMark.draw(context, brandX - 25, 8, 18, Theme.accentColor());
            context.drawText(client.textRenderer, Text.literal(brand), brandX, 8, Theme.brightAccentColor(), true);
            context.drawText(client.textRenderer, Text.literal(tagline), right - taglineWidth, 19, Theme.TEXT_DIM, true);
        } catch (RuntimeException exception) {
            failed = true;
            T1deedynClient.LOGGER.warn("HUD module disabled after render error: {}", exception.toString());
        }
    }
}
