package net.dark.spiritum.mixin.emi$;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.EmiPort;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DefaultedRegistry;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmiPort.class)
public class EmiPortMixin {
    @WrapOperation(method = "getFluidRegistry",at = @At(value = "FIELD", target = "Lnet/minecraft/registry/Registries;FLUID:Lnet/minecraft/registry/DefaultedRegistry;", opcode = Opcodes.GETSTATIC))
    private static DefaultedRegistry<Fluid> spiritum$vatLiquidRegistry(Operation<DefaultedRegistry<Fluid>> original) {
        System.out.println("jaiii");
        return SpiritumRegistries.VAT_LIQUID;
    }
}
