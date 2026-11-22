package net.dark.spiritum.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.LiquidPotion;
import net.dark.spiritum.vat.VatLiquid;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidVariantRendering.class)
public class FluidVariantRenderingMixin {
    @WrapOperation(method = "getTooltip(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;Lnet/minecraft/item/tooltip/TooltipType;)Ljava/util/List;",at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariantAttributes;getName(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;)Lnet/minecraft/text/Text;"))
    private static Text spiritum$correctName(FluidVariant variant, Operation<Text> original) {
        if (variant.getFluid() instanceof LiquidPotion liquidPotion)
            return Text.translatable("item.minecraft.potion.effect."+liquidPotion.getPotion().getBaseName());
        if (variant.getFluid() instanceof VatLiquid)
            return Text.translatable(SpiritumRegistries.VAT_LIQUID.getId(variant.getFluid()).toTranslationKey("vat_liquid"));
        return original.call(variant);
    }
}
