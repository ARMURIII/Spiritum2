package net.dark.spiritum.item;

import net.dark.spiritum.magic.RingBearer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RingItem extends Item {
    public RingItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setStackInHand(hand,((RingBearer) user).spiritum$swapRing(user.getStackInHand(hand)));
        return ActionResult.SUCCESS;
    }
}
