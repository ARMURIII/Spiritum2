package net.dark.spiritum.client.compat;

import mezz.jei.api.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.joml.Matrix3x2f;

import java.util.List;

@JeiPlugin
public final class SpiritumJeiPlugin implements IModPlugin {
    @SuppressWarnings("unused")
    private static mezz.jei.api.runtime.IJeiRuntime runtime;

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime available) {
        runtime = available;
        hideUpgradeRecipe(
                available.getRecipeManager(),
                mezz.jei.api.constants.RecipeTypes.CRAFTING,
                "exalted_ingot");
        hideUpgradeRecipe(
                available.getRecipeManager(),
                mezz.jei.api.constants.RecipeTypes.SMITHING,
                "retrosonus");
    }

    private static <T extends net.minecraft.recipe.Recipe<?>> void hideUpgradeRecipe(
            mezz.jei.api.recipe.IRecipeManager manager,
            mezz.jei.api.recipe.types.IRecipeHolderType<T> type,
            String id) {
        var recipes =
                manager.createRecipeLookup(type)
                        .includeHidden()
                        .get()
                        .filter(recipe -> recipe.id().getValue().equals(ModContent.id(id)))
                        .toList();
        manager.hideRecipes(type, recipes);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static final IRecipeType<MagicRecipeView> RITUALS =
            IRecipeType.create("spiritum", "rituals", MagicRecipeView.class);
    public static final IRecipeType<MagicRecipeView> ALCHEMY =
            IRecipeType.create("spiritum", "alchemy", MagicRecipeView.class);

    @Override
    public Identifier getPluginUid() {
        return ModContent.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(false), new Category(true));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RITUALS, MagicRecipeView.rituals());
        registration.addRecipes(ALCHEMY, MagicRecipeView.alchemyRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RITUALS, new ItemStack(ModContent.RITUAL_PEDESTAL), new ItemStack(ModContent.HEXED_CANDLE));
        registration.addCraftingStation(ALCHEMY, new ItemStack(ModContent.ALCHEMY_VAT));
    }

    private record Category(boolean alchemy) implements IRecipeCategory<MagicRecipeView> {

        @Override
            public IRecipeType<MagicRecipeView> getRecipeType() {
                return alchemy ? ALCHEMY : RITUALS;
            }

            @Override
            public Text getTitle() {
                return Text.translatable(
                    alchemy ? "viewer.spiritum.alchemy" : "viewer.spiritum.rituals");
            }

            @Override
            public int getWidth() {
                return 190;
            }

            @Override
            public int getHeight() {
                return 150;
            }

            @Override
            public IDrawable getIcon() {
                return null;
            }

            @Override
            public Identifier getIdentifier(MagicRecipeView recipe) {
                //if (IRecipeCategory.super.getIdentifier(recipe) != null)
                return IRecipeCategory.super.getIdentifier(recipe);
                //return Identifier.of(Spiritum.MOD_ID, recipe.recipe().id());
            }

            @Override
            public void setRecipe(
                IRecipeLayoutBuilder builder, MagicRecipeView view, IFocusGroup focuses) {
                var inputs = view.inputs();
                for (int i = 0; i < inputs.size(); i++)
                    builder.addSlot(RecipeIngredientRole.INPUT, 4 + (i % 4) * 24, 22 + (i / 4) * 24)
                        .addItemStacks(List.of(inputs.get(i)));

                var fluid = view.fluid();
                if (fluid != Fluids.EMPTY)
                    builder.addSlot(RecipeIngredientRole.INPUT, 4, 46)
                        .add(fluid, 1);

                var spirits = view.spirits();
                if (spirits.size() == 1)
                    builder.addSlot(RecipeIngredientRole.INPUT, 4 + 38, 46)
                        .addItemStacks(List.of(spirits.getFirst()));
                else
                    for (int i = 0; i < spirits.size(); i++) {
                        if (spirits.get(i).isOf(ModContent.SPIRIT_GEM) && view.recipe().ownerCandles().minInclusive() != view.recipe().ownerCandles().maxInclusive()) {
                            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i * 96 / (spirits.size() - 1), 46)
                                .add(spirits.get(i));
                        } else
                            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i * 96 / (spirits.size() - 1), 46)
                                .addItemStacks(List.of(spirits.get(i)));
                    }

                if (!view.output().isEmpty())
                    builder.addSlot(RecipeIngredientRole.OUTPUT, 160, 34)
                        .addItemStacks(List.of(view.output()));
            }

            @Override
            public void draw(
                MagicRecipeView view,
                IRecipeSlotsView slots,
                DrawContext draw,
                double mouseX,
                double mouseY) {
                var font = MinecraftClient.getInstance().textRenderer;
                draw.drawText(font, view.title(), 2, 2, 0xff555555, false);

                var spirits = view.spirits();
                if (spirits.isEmpty()) ; // skips if empty
                else if (spirits.size() == 1)
                    drawCandle(38, 53, draw);
                else
                    for (int i = 0; i < spirits.size(); i++)
                        drawCandle(i * 96 / (spirits.size() - 1), 53, draw);


                if (!view.output().isEmpty()) draw.drawText(font, "→", 130, 38, 0xff555555, false);
                int y = 76;
                for (Text note : view.notes()) {
                    for (var line : font.wrapLines(note, 186)) {
                        draw.drawText(font, line, 2, y, 0xff555555, false);
                        y += 10;
                    }
                }
            }

            private static void drawCandle(int x, int y, DrawContext draw) {
                var stack = ModContent.HEXED_CANDLE.asItem().getDefaultStack();
                if (!stack.isEmpty()) {
                    KeyedItemRenderState keyedItemRenderState = new KeyedItemRenderState();
                    MinecraftClient.getInstance().getItemModelManager().clearAndUpdate(keyedItemRenderState, stack, ItemDisplayContext.GUI,
                        MinecraftClient.getInstance().world,
                        MinecraftClient.getInstance().player,
                        0
                    );

                    try {
                        draw.state.addItem(new ItemGuiElementRenderState(stack.getItem().getName().toString(),
                            new Matrix3x2f(draw.getMatrices()).scale(1.5f), keyedItemRenderState, (int) (x / 1.5f), (int) (y / 1.5f), draw.scissorStack.peekLast()));
                    } catch (Throwable throwable) {
                        CrashReport crashReport = CrashReport.create(throwable, "Rendering item");
                        CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                        crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                        crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                        crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                        throw new CrashException(crashReport);
                    }
                }
            }
        }
}
