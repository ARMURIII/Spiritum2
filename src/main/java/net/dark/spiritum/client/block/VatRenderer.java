package net.dark.spiritum.client.block;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.dark.spiritum.block.entity.VatBlockEntity;
import net.dark.spiritum.client.AnimationAngles;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import java.util.ArrayList;
import java.util.List;

public class VatRenderer
        implements BlockEntityRenderer<VatBlockEntity, VatRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Fluid fluid;
        int color;
        float rotation;
        float height;
        final List<ItemRenderState> items = new ArrayList<>();
        BlockRenderView blockRenderView;
    }

    private final ItemModelManager models;

    public VatRenderer(BlockEntityRendererFactory.Context context) {
        models = context.itemModelManager();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(
            VatBlockEntity vat,
            State state,
            float tickProgress,
            Vec3d cameraPos,
            ModelCommandRenderer.CrumblingOverlayCommand overlay) {

        BlockEntityRenderer.super.updateRenderState(
                vat, state, tickProgress, cameraPos, overlay
        );
        state.blockRenderView = vat.getWorld();
        state.fluid = vat.getFluid() == Fluids.EMPTY ? vat.getPrevFluid() : vat.getFluid();
        state.color = vat.getColor();
        state.height = vat.getHeight(tickProgress);
        if (vat.getFluid() == Fluids.EMPTY && vat.getPrevFluid() == Fluids.EMPTY)
            state.height = 0f;
        state.rotation =
            vat.getWorld() == null
                ? 0
                : AnimationAngles.pedestal(vat.getWorld().getTime(), tickProgress)*2;
        state.items.clear();
        // Keep every stack in storage; display up to 16 representatives to bound rendering costs.
        forItem: for (int i = 0; i < vat.getOfferings().size(); i++) {
            for (int j = 0; j < Math.max(1, vat.getOfferings().stream().mapToInt(ItemStack::getCount).sum() > 16 ?
                (vat.getOfferings().get(i).getCount()*16/vat.getOfferings().stream().mapToInt(ItemStack::getCount).sum()) : vat.getOfferings().get(i).getCount()
            ); j++) {
                ItemRenderState item = new ItemRenderState();
                models.clearAndUpdate(
                    item,
                    vat.getOfferings().get(i).copyWithCount(1),
                    ItemDisplayContext.GROUND,
                    vat.getWorld(),
                    null,
                    i);
                state.items.add(item);
                if (state.items.size()>15)
                    break forItem;
            }
        }
    }

    @Override
    public void render(
        State state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState camera) {

        if (state.height == 0f) {
            /*matrices.push();
            renderItems(state, matrices, queue, camera);
            matrices.pop();*/
            return;
        }

        matrices.push();

        FluidVariant fluidVariant = FluidVariant.of(state.fluid);

        queue.submitCustom(matrices, RenderLayers.itemEntityTranslucentCull(FluidVariantRendering.getSprite(fluidVariant).getAtlasId()),
            (pose, vertexConsumer) -> {
            var color = state.color == -1 || state.fluid != Fluids.WATER ? FluidVariantRendering.getColor(fluidVariant,state.blockRenderView,state.pos) : state.color+0xFF000000;
            var sprite = FluidVariantRendering.getSprite(fluidVariant);

            if (FabricLoader.getInstance().isModLoaded("sodium"))
                SpriteUtil.INSTANCE.markSpriteActive(sprite); // else the sprite is not animated

            var minU = sprite.getFrameU(2/16f);
            var maxU = sprite.getFrameU(14/16f);
            var minV = sprite.getFrameV(2/16f);
            var maxV = sprite.getFrameV(14/16f);

            var light = state.fluid == Fluids.LAVA ? LightmapTextureManager.MAX_LIGHT_COORDINATE : state.lightmapCoordinates;

            var waveFactor = 0.03f;

            vertexConsumer.vertex(pose, 2/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin(state.rotation/360*MathHelper.TAU)*waveFactor),
                    2/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(minU,minV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,2/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+90)/360*MathHelper.TAU)*waveFactor),
                    14/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(minU,maxV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,14/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+180)/360*MathHelper.TAU)*waveFactor),
                    14/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(maxU,maxV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,14/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+270)/360*MathHelper.TAU)*waveFactor),
                    2/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(maxU,minV).light(light).normal(1,1,1);
        });

        matrices.pop();
    }

    /*private void renderItems(State state,
                             MatrixStack matrices,
                             OrderedRenderCommandQueue queue,
                             CameraRenderState camera) {
        for (int i = 0; i < state.items.size(); i++) {
            double angle = 2 * Math.PI * i / state.items.size();
            double radius = state.items.size() == 1 ? 0 : .24;
            matrices.push();
            matrices.translate(
                .5 + Math.cos(angle) * radius,
                0.45,
                .5 + Math.sin(angle) * radius);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (-angle+Math.PI/2))
                .rotateX((float)Math.toRadians(22.5))
            );
            matrices.scale(.55f, .55f, .55f);
            state.items
                .get(i)
                .render(
                    matrices,
                    queue,
                    state.lightmapCoordinates,
                    OverlayTexture.DEFAULT_UV,
                    0);
            matrices.pop();
        }
    }*/
}
