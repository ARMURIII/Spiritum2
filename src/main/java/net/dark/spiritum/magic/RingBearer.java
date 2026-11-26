package net.dark.spiritum.magic;

import net.minecraft.item.ItemStack;

public interface RingBearer {
    ItemStack spiritum$getRing();

    void spiritum$setRing(ItemStack stack);

    default ItemStack spiritum$swapRing(ItemStack stack) {
        var old = spiritum$getRing();
        spiritum$setRing(stack);
        return old;
    }
}
