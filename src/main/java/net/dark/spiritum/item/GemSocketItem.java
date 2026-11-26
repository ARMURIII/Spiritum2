package net.dark.spiritum.item;

import net.dark.spiritum.magic.InteractionEffects;
import net.dark.spiritum.registry.SpiritumContent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;

public class GemSocketItem extends Item {
    public GemSocketItem(Settings settings) {
        super(settings);
    }

    protected boolean acceptsGem(ItemStack stack) { return stack.isOf(SpiritumContent.SPIRIT_GEM); }

    protected boolean canRemoveGem() { return true; }

    protected SoundEvent placeSound() {
        return SoundEvents.BLOCK_STONE_PLACE;
    }

    protected SoundEvent removeSound() {
        return SoundEvents.BLOCK_STONE_BREAK;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new GemSocketTooltipData(SpiritBinding.socket(stack)));
    }

    @Override
    public boolean onClicked(ItemStack holder, ItemStack cursor, Slot slot, ClickType click, PlayerEntity player, StackReference cursorReference) {
        if (!slot.canTakePartial(player)) return false;
        ItemStack gem = SpiritBinding.socket(holder);
        if (click == ClickType.LEFT && gem.isEmpty() && acceptsGem(cursor)) {
            SpiritBinding.socket(holder, cursor);
            cursor.decrement(1);
            player.playSound(placeSound(), .8f, 2);
            slot.markDirty();
            return true;
        }
        if (canRemoveGem() && click == ClickType.RIGHT && cursor.isEmpty() && !gem.isEmpty()) {
            cursorReference.set(gem);
            SpiritBinding.socket(holder, ItemStack.EMPTY);
            player.playSound(removeSound(), .8f, 2);
            slot.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean onStackClicked(ItemStack holder, Slot slot, ClickType click, PlayerEntity player) {
        ItemStack gem = SpiritBinding.socket(holder);
        if (click == ClickType.LEFT && gem.isEmpty() && acceptsGem(slot.getStack())) {
            ItemStack taken = slot.takeStackRange(1, 1, player);
            if (taken.isEmpty()) return false;
            SpiritBinding.socket(holder, taken);
            player.playSound(placeSound(), .8f, 2);
            return true;
        }
        if (canRemoveGem() && click == ClickType.RIGHT && !gem.isEmpty() && slot.getStack().isEmpty() && slot.canInsert(gem)) {
            if (!slot.insertStack(gem.copy()).isEmpty()) return false;
            SpiritBinding.socket(holder, ItemStack.EMPTY);
            player.playSound(removeSound(), .8f, 2);
            return true;
        }
        return false;
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);
        var weapon = stack.get(DataComponentTypes.WEAPON);
        if (weapon != null
                && stack.getDamage() + weapon.itemDamagePerAttack() >= stack.getMaxDamage()
                && !attacker.getEntityWorld().isClient()) {
            ItemStack gem = SpiritBinding.socket(stack);
            if (!gem.isEmpty()) {
                SpiritBinding.socket(stack, ItemStack.EMPTY);
                if (attacker instanceof PlayerEntity player) player.getInventory().offerOrDrop(gem);
                else
                    attacker.getEntityWorld()
                            .spawnEntity(
                                    new ItemEntity(
                                            attacker.getEntityWorld(),
                                            attacker.getX(),
                                            attacker.getY(),
                                            attacker.getZ(),
                                            gem));
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack holder = user.getStackInHand(hand);
        ItemStack gem = SpiritBinding.socket(holder);
        if (canRemoveGem() && user.isSneaking() && !gem.isEmpty()) {
            if (!world.isClient()) {
                SpiritBinding.socket(holder, ItemStack.EMPTY);
                InteractionEffects.atHand(user, false);
                user.getInventory().offerOrDrop(gem);

            }
            return ActionResult.SUCCESS;
        }
        ItemStack other =
                user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (!gem.isEmpty() || !acceptsGem(other)) return ActionResult.PASS;
        if (!world.isClient()) {
            SpiritBinding.socket(holder, other);
            InteractionEffects.atHand(user, true);
            if (!user.isCreative()) other.decrement(1);
        }
        return ActionResult.SUCCESS;
    }
}
