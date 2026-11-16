package net.dark.spiritum.client.compat;

import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.text.Text;

import java.util.*;

public record MagicRecipeView(OfferingRecipe recipe, boolean alchemy) {
    public List<ItemStack> inputs() {
        List<ItemStack> inputs = new ArrayList<>();
        recipe.ingredients().forEach((item, count) -> inputs.add(new ItemStack(item, count)));
        return inputs;
    }

    public Fluid fluid() {
        return alchemy ? recipe.fluid() : Fluids.EMPTY;
    }

    public List<ItemStack> spirits() {
        List<ItemStack> inputs = new ArrayList<>();
        int count = recipe.ownerCandles().maxInclusive();

        for (int i = 0; i < recipe.candles()-(RitualCandles.requiresOwner(recipe)?1:0); i++)
            inputs.add(new ItemStack(ModContent.SPIRIT_FRAGMENT));

        if (count>0) {
            for (int i = 0; i < count; i++) {
                ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
                NbtComponent.set(
                    DataComponentTypes.CUSTOM_DATA,
                    gem,
                    nbt -> {
                        nbt.putString("SpiritumPlayer", UUID.randomUUID().toString());
                        nbt.putString("SpiritumPlayerName", "Desired Player");
                    });
                gem.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

                inputs.add(gem);
            }
        }

        return inputs;
    }

    public ItemStack output() {
        return recipe.outputStack();
    }

    public Text title() {
        return alchemy ? output().getName() : Text.translatable("ritual.spiritum." + recipe.id());
    }

    public List<Text> notes() {
        if (alchemy)
            return List.of(
                    Text.translatable("viewer.spiritum.boiling_water"),
                    Text.translatable("viewer.spiritum.vat_instant"));

        List<Text> notes = new ArrayList<>();
        if (RitualCandles.requiresOwner(recipe) || (recipe.ownerCandles().minInclusive() == recipe.ownerCandles().maxInclusive() && recipe.ownerCandles().minInclusive() != 0))
            notes.add(Text.translatable("viewer.spiritum.bound_candles", recipe.candles()-recipe.ownerCandles().minInclusive(),recipe.ownerCandles().minInclusive()));
        else if (recipe.ownerCandles().maxInclusive()>0)
            notes.add(Text.translatable("viewer.spiritum.optional_candles", recipe.candles()-recipe.ownerCandles().minInclusive(),recipe.ownerCandles().minInclusive(),recipe.ownerCandles().maxInclusive()));
        else notes.add(Text.translatable("viewer.spiritum.candles", recipe.candles()));
        notes.add(
                Text.translatable(
                        recipe.persistent()
                                ? "viewer.spiritum.persistent"
                                : "viewer.spiritum.instant"));
        notes.add(Text.translatable("viewer.spiritum.effect." + recipe.id()));
        if (recipe.id().endsWith("_binding"))
            notes.add(Text.translatable("viewer.spiritum.binding_return"));
        else if (recipe.id().equals("withering"))
            notes.add(Text.translatable("viewer.spiritum.bound_protection"));
        return notes;
    }

    public static List<MagicRecipeView> rituals() {
        return MagicRecipes.RITUALS.stream().map(r -> new MagicRecipeView(r, false)).toList();
    }

    public static List<MagicRecipeView> alchemyRecipes() {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return List.of();
        return world.getRecipeManager().getSynchronizedRecipes().getAllOfType(AlchemyRecipe.TYPE)
                .stream().map(entry -> new MagicRecipeView(
                        entry.value().offering(entry.id().getValue().toString()), true)).toList();
    }
}
