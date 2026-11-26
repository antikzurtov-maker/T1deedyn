package dev.t1deedyn;

import java.util.List;

/** Describes a real setting already backed by ModConfig. */
public final class CloverFeature {
    public final int tab;
    public final String name;
    public final String description;
    private final int key;

    private CloverFeature(int tab, String name, String description, int key) {
        this.tab = tab;
        this.name = name;
        this.description = description;
        this.key = key;
    }

    public boolean enabled() {
        return switch (key) {
            case 0 -> ModConfig.particles;
            case 1 -> ModConfig.lowFire;
            case 2 -> ModConfig.smoothCamera;
            case 3 -> ModConfig.skyVisual;
            case 4 -> ModConfig.rgbLighting;
            case 5 -> ModConfig.autoBerryEnabled;
            case 6 -> ModConfig.menuAnimations;
            case 7 -> ModConfig.cleanHud;
            case 8 -> ModConfig.showCoordinates;
            case 9 -> ModConfig.showFps;
            case 10 -> ModConfig.showPing;
            case 11 -> ModConfig.showHudCoordinates;
            case 12 -> ModConfig.autoBerryCountHud;
            case 13 -> ModConfig.autoBerryParticles;
            case 14 -> ModConfig.autoBerryHighlight;
            case 15 -> ModConfig.berryHighlightDebug;
            case 16 -> ModConfig.autoPerformance;
            default -> false;
        };
    }

    public void setEnabled(boolean value) {
        switch (key) {
            case 0 -> ModConfig.particles = value;
            case 1 -> ModConfig.lowFire = value;
            case 2 -> ModConfig.smoothCamera = value;
            case 3 -> ModConfig.skyVisual = value;
            case 4 -> ModConfig.rgbLighting = value;
            case 5 -> ModConfig.autoBerryEnabled = value;
            case 6 -> ModConfig.menuAnimations = value;
            case 7 -> ModConfig.cleanHud = value;
            case 8 -> ModConfig.showCoordinates = value;
            case 9 -> ModConfig.showFps = value;
            case 10 -> ModConfig.showPing = value;
            case 11 -> ModConfig.showHudCoordinates = value;
            case 12 -> ModConfig.autoBerryCountHud = value;
            case 13 -> ModConfig.autoBerryParticles = value;
            case 14 -> ModConfig.autoBerryHighlight = value;
            case 15 -> ModConfig.berryHighlightDebug = value;
            case 16 -> ModConfig.autoPerformance = value;
            default -> { return; }
        }
        ModConfig.save();
    }

    public static final List<CloverFeature> ALL = List.of(
        new CloverFeature(0, "Частицы", "Отображение частиц окружения", 0),
        new CloverFeature(0, "Низкий огонь", "Уменьшенная высота огня от первого лица", 1),
        new CloverFeature(0, "Плавная камера", "Дополнительная плавность движения камеры", 2),
        new CloverFeature(0, "Визуальное небо", "Дополнительное оформление неба", 3),
        new CloverFeature(0, "RGB-подсветка", "Цветовая подсветка интерфейсных элементов", 4),
        new CloverFeature(1, "AutoBerry", "Автоматический сбор созревших ягод", 5),
        new CloverFeature(3, "Анимации меню", "Плавное появление элементов интерфейса", 6),
        new CloverFeature(3, "Чистый HUD", "Упрощённое отображение игрового интерфейса", 7),
        new CloverFeature(4, "Координаты", "Показывать координаты игрока", 8),
        new CloverFeature(4, "Счётчик FPS", "Показывать частоту кадров", 9),
        new CloverFeature(4, "Пинг", "Показывать сетевую задержку", 10),
        new CloverFeature(4, "Координаты HUD", "Координаты в информационном HUD мода", 11),
        new CloverFeature(4, "Счётчик AutoBerry", "Показывать количество собранных ягод", 12),
        new CloverFeature(5, "Частицы AutoBerry", "Эффект при успешном сборе", 13),
        new CloverFeature(5, "Подсветка ягод", "Подсвечивать найденные ягодные кусты", 14),
        new CloverFeature(5, "Отладка подсветки", "Показывать диагностические метки подсветки", 15),
        new CloverFeature(6, "Автопроизводительность", "Ограничивать нагрузку визуальных эффектов", 16)
    );

    private CloverFeature() { throw new AssertionError(); }
}
