package net.dark.spiritum.item;

import net.dark.spiritum.registry.SpiritumContent;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class RetrosonusItem extends TunedBladeItem {
    public RetrosonusItem(Settings settings) {
        super(settings);
    }

    @Override
    protected SoundEvent placeSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE;
    }

    @Override
    protected SoundEvent removeSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
    }

    @Override
    protected boolean acceptsGem(ItemStack stack) {
        return stack.isOf(SpiritumContent.SPIRIT_GEM);
    }
}
