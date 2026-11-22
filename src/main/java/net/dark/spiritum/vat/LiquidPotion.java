package net.dark.spiritum.vat;

import net.dark.spiritum.registry.SpiritumRegistries;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.jspecify.annotations.Nullable;

public class LiquidPotion extends WaterFluid.Still {

    private final Potion potion;

    private final RegistryEntry.Reference<Fluid> registryEntry = SpiritumRegistries.VAT_LIQUID.createEntry(this);

    public LiquidPotion(Potion potion) {
        super();
        this.potion = potion;
    }

    @Override
    public RegistryEntry.Reference<Fluid> getRegistryEntry() {
        return registryEntry;
    }

    @Override
    public boolean isStill(FluidState state) {
        return super.isStill(state);
    }

    public LiquidPotion() {
        this(Potions.WATER.value());
    }

    @Override
    public Item getBucketItem() {
        return Items.AIR;
    }

    public Potion getPotion() {
        return potion;
    }

    @Override
    public @Nullable ParticleEffect getParticle() {
        return null;
    }

    public static Fluid ofPotion(RegistryEntry<Potion> potion) {
        if (potion == Potions.WATER)
            return Fluids.WATER;
        return SpiritumRegistries.VAT_LIQUID.get(Registries.POTION.getId(potion.value()));
    }

    public static RegistryEntry<Potion> ofFluid(Fluid fluid) {
        if (fluid == Fluids.WATER)
            return Potions.WATER;
        return fluid instanceof LiquidPotion potionLiquid ? Registries.POTION.getEntry(potionLiquid.potion) : null;
    }
}
