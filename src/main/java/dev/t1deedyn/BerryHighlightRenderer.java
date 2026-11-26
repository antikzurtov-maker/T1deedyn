package dev.t1deedyn;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CaveVinesBodyBlock;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Independent, tick-cached ripe berry locator and world overlay. */
public final class BerryHighlightRenderer {
    private static final int LIMIT = 64;
    private static final long[] POSITIONS = new long[LIMIT];
    private static final BlockPos.Mutable CURSOR = new BlockPos.Mutable();
    private static int count;
    private static long ticks;
    private static World lastWorld;

    private BerryHighlightRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BerryHighlightRenderer::render);
    }

    public static void tick(MinecraftClient client) {
        ticks++;
        if (!ModConfig.autoBerryHighlight) { count=0; lastWorld=null; return; }
        if (client.player==null || client.world==null) { count=0; lastWorld=null; return; }
        if (client.world!=lastWorld) { count=0; lastWorld=client.world; }
        if (ticks%Math.max(1,ModConfig.berryHighlightScanTicks)!=0) return;
        count=0;
        int radius=Math.max(1,Math.min(16,ModConfig.berryHighlightRadius));
        int px=(int)Math.floor(client.player.getX()), py=(int)Math.floor(client.player.getY()), pz=(int)Math.floor(client.player.getZ());
        int r2=radius*radius;
        World world=client.world;
        for(int x=px-radius;x<=px+radius && count<LIMIT;x++) {
            for(int y=py-radius;y<=py+radius && count<LIMIT;y++) {
                for(int z=pz-radius;z<=pz+radius && count<LIMIT;z++) {
                    int dx=x-px,dy=y-py,dz=z-pz;
                    if(dx*dx+dy*dy+dz*dz>r2) continue;
                    CURSOR.set(x,y,z);
                    if(!world.isChunkLoaded(CURSOR)) continue;
                    BlockState state=world.getBlockState(CURSOR);
                    if(isRipe(state)) POSITIONS[count++]=BlockPos.asLong(x,y,z);
                }
            }
        }
    }

    private static boolean isRipe(BlockState state) {
        if(state.getBlock() instanceof SweetBerryBushBlock)
            return ModConfig.autoBerrySweet && state.get(SweetBerryBushBlock.AGE)>=3;
        if(state.getBlock() instanceof CaveVinesBodyBlock || state.getBlock() instanceof CaveVinesHeadBlock)
            return ModConfig.autoBerryGlow && state.get(CaveVines.BERRIES);
        return false;
    }

    private static void render(WorldRenderContext context) {
        if(!ModConfig.autoBerryHighlight || count==0 || context.world()==null || context.matrixStack()==null || context.consumers()==null) return;
        try {
            MatrixStack matrices=context.matrixStack();
            Vec3d camera=context.camera().getPos();
            VertexConsumer lines=context.consumers().getBuffer(RenderLayer.getLines());
            int rgb=ModConfig.berryHighlightColor;
            float red=((rgb>>16)&255)/255.0f, green=((rgb>>8)&255)/255.0f, blue=(rgb&255)/255.0f;
            matrices.push();
            matrices.translate(-camera.x,-camera.y,-camera.z);
            for(int i=0;i<count;i++) {
                long packed=POSITIONS[i];
                int x=BlockPos.unpackLongX(packed), y=BlockPos.unpackLongY(packed), z=BlockPos.unpackLongZ(packed);
                WorldRenderer.drawBox(matrices,lines,x+0.04,y+0.04,z+0.04,x+0.96,y+0.96,z+0.96,red,green,blue,0.95f);
            }
            matrices.pop();
        } catch (RuntimeException exception) {
            T1deedynClient.LOGGER.error("Berry highlight world render failed",exception);
        }
    }

    public static int count() { return count; }
}
