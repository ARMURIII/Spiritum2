package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.RingBearer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerRingMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void spiritum$copy(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ((RingBearer) player).spiritum$setRing(((RingBearer) oldPlayer).spiritum$getRing().copy());
    }
}
