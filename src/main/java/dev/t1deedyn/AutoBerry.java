package dev.t1deedyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CaveVinesBodyBlock;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;

/**
 * Client-side berry collector. It only sends normal block-interaction packets;
 * it never invokes block-breaking APIs.
 */
public final class AutoBerry extends VisualModule {
    private static final int MAX_TARGETS = 64;
    private static final BlockPos.Mutable SCAN_POS = new BlockPos.Mutable();
    private static final ArrayList<BlockPos> TARGETS = new ArrayList<>(MAX_TARGETS);
    private static final double[] TARGET_DISTANCES = new double[MAX_TARGETS];
    private static final HashMap<Long, Long> RECENTLY_CLICKED = new HashMap<>();
    private static final HashMap<Long, Long> BLACKLIST = new HashMap<>();
    private static final HashMap<Long, Integer> FAILURES = new HashMap<>();
    private static final Random RANDOM = Random.create();

    private static final double[] EYE = new double[3];
    private static long tickCounter;
    private static long nextClickTick;
    private static long lastWarningTick = -1000;
    private static long cpsWindowStart;
    private static int clicksInWindow;
    private static int collected;
    private static int lastScanTick = -1000;
    private static Object lastWorld;
    private static RegistryKey<World> lastDimension;
    private static double previousX, previousY, previousZ;
    private static boolean havePreviousPosition;
    private static boolean errorLogged;

    public AutoBerry() {
        super("AutoBerry");
    }

    public static void registerTick() {
        ClientTickEvents.END_CLIENT_TICK.register(AutoBerry::tick);
    }

    private static void tick(MinecraftClient client) {
        tickCounter++;
        try {
            if (!ModConfig.autoBerryEnabled) {
                clearRuntimeStateIfNeeded();
                return;
            }

            ClientPlayerEntity player = client.player;
            World world = client.world;
            if (player == null || world == null || player.isSpectator() || client.interactionManager == null) {
                resetWorldState();
                return;
            }

            if (world != lastWorld || !world.getRegistryKey().equals(lastDimension)) {
                resetWorldState();
                lastWorld = world;
                lastDimension = world.getRegistryKey();
                collected = 0;
            }
            if (player.isDead() || player.getHealth() <= 0.0f) {
                resetWorldState();
                return;
            }
            if (ModConfig.autoBerryOnlyStationary) {
                double dx = player.getX() - previousX;
                double dy = player.getY() - previousY;
                double dz = player.getZ() - previousZ;
                if (havePreviousPosition && dx * dx + dy * dy + dz * dz > 0.0004) {
                    rememberPosition(player);
                    return;
                }
            }
            rememberPosition(player);
            if (ModConfig.autoBerryPauseWithScreen && client.currentScreen != null) return;
            if (ModConfig.autoBerryPauseUsingItem && player.isUsingItem()) return;
            if (ModConfig.autoBerrySafeHand && isUnsafeStack(player.getMainHandStack())) {
                if (tickCounter - lastWarningTick >= 100) {
                    player.sendMessage(net.minecraft.text.Text.literal("AutoBerry: смени предмет в руке"), true);
                    lastWarningTick = tickCounter;
                }
                return;
            }

            if (tickCounter % 20 == 0) {
                cleanExpiredMaps(world, player);
            }

            if (tickCounter - lastScanTick >= ModConfig.autoBerryScanTicks) {
                scan(client, player, world);
                lastScanTick = tickCounter;
            }
            if (TARGETS.isEmpty() || tickCounter < nextClickTick) return;

            if (tickCounter - cpsWindowStart >= 20) {
                cpsWindowStart = tickCounter;
                clicksInWindow = 0;
            }
            if (clicksInWindow >= ModConfig.autoBerryMaxCps) return;

            BlockPos target = TARGETS.get(0);
            BlockState state = world.getBlockState(target);
            if (!isReadyBerry(state)) {
                TARGETS.remove(0);
                return;
            }

            Vec3d eye = player.getEyePos();
            Direction face = nearestFace(target, eye);
            Vec3d hit = faceCenter(target, face);
            BlockHitResult hitResult = new BlockHitResult(hit, face, target, false);
            ActionResult result = client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
            if (result.isAccepted()) {
                if (ModConfig.autoBerrySwing) player.swingHand(Hand.MAIN_HAND);
                long key = target.asLong();
                RECENTLY_CLICKED.put(key, tickCounter + 20);
                TARGETS.remove(0);
                clicksInWindow++;
                nextClickTick = tickCounter + ModConfig.autoBerryClickDelay
                        + (ModConfig.autoBerryRandomDelay > 0 ? RANDOM.nextInt(ModConfig.autoBerryRandomDelay + 1) : 0);
                if (ModConfig.autoBerryCountHud) collected++;
                if (ModConfig.autoBerryParticles) {
                    client.world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                            target.getX() + 0.5, target.getY() + 0.65, target.getZ() + 0.5,
                            0.0, 0.025, 0.0);
                }
            } else {
                nextClickTick = tickCounter + 1;
            }
        } catch (Throwable throwable) {
            ModConfig.autoBerryEnabled = false;
            ModConfig.save();
            TARGETS.clear();
            RECENTLY_CLICKED.clear();
            BLACKLIST.clear();
            FAILURES.clear();
            if (!errorLogged) {
                errorLogged = true;
                T1deedynClient.LOGGER.error("AutoBerry disabled after an internal error", throwable);
                if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal("AutoBerry отключён из-за ошибки. Подробности в latest.log"), false);
                }
            }
        }
    }

    private static void scan(MinecraftClient client, ClientPlayerEntity player, World world) {
        TARGETS.clear();
        Vec3d eye = player.getEyePos();
        EYE[0] = eye.x; EYE[1] = eye.y; EYE[2] = eye.z;
        double radius = ModConfig.autoBerryRadius;
        if (ModConfig.autoBerryLimitReach) radius = Math.min(radius, player.getBlockInteractionRange() + 0.15);
        double radiusSq = radius * radius;
        int minX = (int)Math.floor(eye.x - radius), maxX = (int)Math.floor(eye.x + radius);
        int minY = (int)Math.floor(eye.y - radius), maxY = (int)Math.floor(eye.y + radius);
        int minZ = (int)Math.floor(eye.z - radius), maxZ = (int)Math.floor(eye.z + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    SCAN_POS.set(x, y, z);
                    if (!world.isChunkLoaded(SCAN_POS)) continue;
                    BlockState state = world.getBlockState(SCAN_POS);
                    if (!isReadyBerry(state)) continue;
                    double nearestX = clamp(EYE[0], x, x + 1.0);
                    double nearestY = clamp(EYE[1], y, y + 1.0);
                    double nearestZ = clamp(EYE[2], z, z + 1.0);
                    double dx = EYE[0] - nearestX, dy = EYE[1] - nearestY, dz = EYE[2] - nearestZ;
                    double distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > radiusSq) continue;
                    long key = BlockPos.asLong(x, y, z);
                    if (RECENTLY_CLICKED.containsKey(key) || BLACKLIST.containsKey(key)) continue;
                    if (ModConfig.autoBerryVisibleOnly) {
                        Vec3d hit = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
                        BlockHitResult ray = world.raycast(new RaycastContext(eye, hit,
                                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
                        if (!ray.getBlockPos().equals(SCAN_POS)) continue;
                    }
                    insertTarget(new BlockPos(x, y, z), distanceSq);
                    if (ModConfig.autoBerryHighlight) {
                        world.addParticle(ParticleTypes.HAPPY_VILLAGER, x + 0.5, y + 0.8, z + 0.5, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    private static void insertTarget(BlockPos pos, double distanceSq) {
        if (TARGETS.size() == MAX_TARGETS && distanceSq >= TARGET_DISTANCES[MAX_TARGETS - 1]) return;
        int index = TARGETS.size();
        if (index == MAX_TARGETS) index--;
        while (index > 0 && TARGET_DISTANCES[index - 1] > distanceSq) {
            if (index < MAX_TARGETS) {
                TARGET_DISTANCES[index] = TARGET_DISTANCES[index - 1];
                if (index < TARGETS.size()) TARGETS.set(index, TARGETS.get(index - 1));
            }
            index--;
        }
        if (TARGETS.size() < MAX_TARGETS) TARGETS.add(pos);
        else TARGETS.set(index, pos);
        TARGET_DISTANCES[index] = distanceSq;
    }

    private static boolean isReadyBerry(BlockState state) {
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            if (!ModConfig.autoBerrySweet) return false;
            int age = state.get(SweetBerryBushBlock.AGE);
            return age >= (ModConfig.autoBerryStageTwo ? 2 : 3);
        }
        if (state.getBlock() instanceof CaveVinesBodyBlock || state.getBlock() instanceof CaveVinesHeadBlock) {
            return ModConfig.autoBerryGlow && state.get(CaveVines.BERRIES);
        }
        return false;
    }

    private static Direction nearestFace(BlockPos pos, Vec3d eye) {
        double dx = eye.x - (pos.getX() + 0.5);
        double dy = eye.y - (pos.getY() + 0.5);
        double dz = eye.z - (pos.getZ() + 0.5);
        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        if (ax >= ay && ax >= az) return dx >= 0 ? Direction.EAST : Direction.WEST;
        if (ay >= az) return dy >= 0 ? Direction.UP : Direction.DOWN;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static Vec3d faceCenter(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        switch (face) {
            case EAST -> x = pos.getX() + 1.0;
            case WEST -> x = pos.getX();
            case UP -> y = pos.getY() + 1.0;
            case DOWN -> y = pos.getY();
            case SOUTH -> z = pos.getZ() + 1.0;
            case NORTH -> z = pos.getZ();
        }
        return new Vec3d(x, y, z);
    }

    private static boolean isUnsafeStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES) return false;
        return item instanceof BoneMealItem || item instanceof BlockItem || item instanceof BucketItem
                || item instanceof FlintAndSteelItem || item instanceof SpawnEggItem
                || item instanceof EnderPearlItem || item instanceof PotionItem
                || item instanceof ProjectileItem || item instanceof RangedWeaponItem
                || item instanceof ShovelItem || item instanceof HoeItem || item instanceof AxeItem
                || item == Items.EGG || item == Items.SNOWBALL || item == Items.FIRE_CHARGE
                || item == Items.EXPERIENCE_BOTTLE;
    }

    private static void cleanExpiredMaps(World world, ClientPlayerEntity player) {
        Iterator<Map.Entry<Long, Long>> recent = RECENTLY_CLICKED.entrySet().iterator();
        while (recent.hasNext()) {
            Map.Entry<Long, Long> entry = recent.next();
            if (entry.getValue() > tickCounter) continue;
            BlockPos pos = BlockPos.fromLong(entry.getKey());
            if (isReadyBerry(world.getBlockState(pos))) {
                int failures = FAILURES.getOrDefault(entry.getKey(), 0) + 1;
                FAILURES.put(entry.getKey(), failures);
                if (failures >= 3) {
                    BLACKLIST.put(entry.getKey(), tickCounter + 100);
                    FAILURES.remove(entry.getKey());
                }
            } else {
                FAILURES.remove(entry.getKey());
            }
            recent.remove();
        }
        Iterator<Map.Entry<Long, Long>> blocked = BLACKLIST.entrySet().iterator();
        while (blocked.hasNext()) if (blocked.next().getValue() <= tickCounter) blocked.remove();
    }

    private static void rememberPosition(ClientPlayerEntity player) {
        previousX = player.getX(); previousY = player.getY(); previousZ = player.getZ();
        havePreviousPosition = true;
    }

    private static void resetWorldState() {
        TARGETS.clear();
        RECENTLY_CLICKED.clear();
        BLACKLIST.clear();
        FAILURES.clear();
        havePreviousPosition = false;
        lastScanTick = -1000;
        lastWorld = null;
        lastDimension = null;
        nextClickTick = tickCounter;
    }

    private static void clearRuntimeStateIfNeeded() {
        if (!TARGETS.isEmpty() || !RECENTLY_CLICKED.isEmpty() || !BLACKLIST.isEmpty() || !FAILURES.isEmpty()) {
            resetWorldState();
        }
    }

    public static int getCollected() { return collected; }
    @Override public boolean isEnabled() { return ModConfig.autoBerryEnabled; }
    @Override public void setEnabled(boolean enabled) { ModConfig.autoBerryEnabled = enabled; ModConfig.save(); resetWorldState(); }

    public static void toggle() {
        ModConfig.autoBerryEnabled = !ModConfig.autoBerryEnabled;
        ModConfig.save();
        resetWorldState();
        errorLogged = false;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
