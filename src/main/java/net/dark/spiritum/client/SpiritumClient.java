package net.dark.spiritum.client;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.client.block.PedestalRenderer;
import net.dark.spiritum.client.block.VatRenderer;
import net.dark.spiritum.event.vanilla.ItemPreTooltipCallback;
import net.dark.spiritum.item.BottleOfHadesItem;
import net.dark.spiritum.item.GemSocketTooltipData;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.item.TunedBladeItem;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpiritumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TunedBladeGuidance.initialize();
        DemonRenderers.initialize();
        SpiritParticles.initialize();
        TooltipComponentCallback.EVENT.register(
                data ->
                        data instanceof GemSocketTooltipData gem
                                ? new GemSocketTooltip(gem)
                                : null);
        BlockEntityRendererFactories.register(ModContent.PEDESTAL_ENTITY, PedestalRenderer::new);
        BlockEntityRendererFactories.register(ModContent.VAT_ENTITY, VatRenderer::new);
        BlockRenderLayerMap.putBlock(ModContent.HEXED_CANDLE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModContent.ALCHEMY_VAT, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tint) ->
                        switch (state.get(HexedCandleBlock.FLAME)) {
                            case 2, 4 -> 0x70E9EF;
                            case 5 -> 0xB291FF;
                            default -> 0xFFFFFF;
                        },
                ModContent.HEXED_CANDLE);
        ItemPreTooltipCallback.BEFORE.register(
                (stack, context, type, lines) -> {
                    var id = Registries.ITEM.getId(stack.getItem());
                    if (!id.getNamespace().equals("spiritum")) return;
                    if (stack.isOf(ModContent.VOODOO_POPPET)
                            || stack.isOf(ModContent.SENTINEL)
                            || stack.getItem() instanceof TunedBladeItem)
                        lines.add(
                            getTooltip(Text.translatable("tooltip.spiritum." + id.getPath())
                                        .formatted(Formatting.GRAY)));
                    if (stack.isOf(ModContent.SUMMONERS_RING)) {
                        lines.add(ScreenTexts.EMPTY);
                        lines.add(
                            Text.translatable("tooltip.spiritum.equipped")
                                .formatted(Formatting.GRAY));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".1")
                                .formatted(Formatting.BLUE));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".2")
                                .formatted(Formatting.BLUE));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".3")
                                .formatted(Formatting.RED));
                    }
                    if (stack.isOf(ModContent.SOULBIND_RING) || stack.isOf(ModContent.WARDING_RING)) {
                        lines.add(ScreenTexts.EMPTY);
                        lines.add(
                            Text.translatable("tooltip.spiritum.equipped")
                                .formatted(Formatting.GRAY));
                        lines.add(
                            getTooltip(Text.translatable("tooltip.spiritum." + id.getPath())
                                .formatted(Formatting.DARK_GRAY)));
                    }
                    var gem =
                            stack.isOf(ModContent.SPIRIT_GEM) ? stack : SpiritBinding.socket(stack);
                    SpiritBinding.player(gem)
                            .ifPresent(
                                    uuid ->
                                            lines.add(
                                                    Text.translatable(
                                                                    "tooltip.spiritum.bound",
                                                                    SpiritBinding.playerName(gem))
                                                            .formatted(Formatting.AQUA)));
                    if (stack.isOf(ModContent.BOTTLE_OF_HADES))
                        lines.add(
                                Text.translatable(
                                                "tooltip.spiritum.bottle_contents",
                                                BottleOfHadesItem.count(stack))
                                        .formatted(Formatting.AQUA));
                });
    }

    public Text getTooltip(Text text) {
        return MinecraftClient.getInstance().isShiftPressed() ? text :
            Text.translatable("tooltip.spiritum.show",Text.translatable("tooltip.spiritum.key").formatted(Formatting.GRAY)).formatted(Formatting.DARK_GRAY);
    }
}
