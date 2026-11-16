package net.dark.spiritum.block.entity;

import net.dark.spiritum.magic.*;
import net.dark.spiritum.mixin.BucketItemAccessor;
import net.dark.spiritum.registry.ModContent;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.minecraft.block.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

@SuppressWarnings("unused")
public class VatBlockEntity extends OfferingBlockEntity {
    private int boilTicks;
    private Fluid fluid = Fluids.EMPTY;
    private Fluid prevFluid;
    private volatile int color = -1;
    private float prevHeight;
    private float height;

    public VatBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.VAT_ENTITY, pos, state);
    }

    public int getBoilTicks() {
        return boilTicks;
    }

    public int getColor() {
        return color;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public Fluid getPrevFluid() {
        return prevFluid;
    }

    @Override
    public void changed() {
        color = calculateColor();
        super.changed();
    }

    private int calculateColor() {
        if (offerings.isEmpty()) return 0x3F76E4;
        int red = 0, green = 0, blue = 0, total = 0;
        for (ItemStack stack : offerings) {
            int color = ingredientColor(stack.getItem());
            int count = stack.getCount();
            red += ((color >> 16) & 255) * count;
            green += ((color >> 8) & 255) * count;
            blue += (color & 255) * count;
            total += count;
        }
        return (total == 0 ? 0xFFFFFF : ((red / total) << 16) | ((green / total) << 8) | (blue / total));
    }

    private static int ingredientColor(Item item) {
        if (item == Items.REDSTONE) return 0xC73643;
        if (item == Items.GLOWSTONE_DUST || item == Items.GLOWSTONE || item == Items.HONEY_BOTTLE) return 0xE8C65B;
        if (item == Items.CHARCOAL) return 0x494253;
        if (item == ModContent.SPIRIT_FRAGMENT || item == ModContent.SPIRIT_GEM) return 0x8DDD64;
        if (item == Items.ROTTEN_FLESH || item == ModContent.LIVING_FLESH) return 0xB67473;
        if (item == ModContent.HEX_ASH || item == ModContent.CALX_OF_HADES) return 0x8F70B2;
        if (item == ModContent.ARGENT_INGOT) return 0xB9D5CF;
        return 0xCEC7AD;
    }

    public float getHeight(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevHeight, this.height);
    }

    public void fill(ItemStack stack) {
        if (world == null) return;
        prevFluid = fluid;
        boilTicks = 600;
        if (stack.getItem() instanceof BucketItem bucketItem) {
            fluid = bucketItem.getFluid();
            if (fluid == Fluids.WATER)
                InteractionEffects.splash(world, pos);
            ((BucketItemAccessor) bucketItem).spiritum$playEmptyingSound(null, world, pos);
        }
        changed();
    }

    public ItemStack empty(ItemStack stack) {
        if (world == null) return stack;
        if (fluid == Fluids.EMPTY) return stack;
        else prevFluid = fluid;
        empty();
        if (prevFluid.getBucketFillSound().isPresent())
            world.playSoundAtBlockCenterClient(pos,prevFluid.getBucketFillSound().get(),SoundCategory.BLOCKS,1f,1f,true);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return prevFluid.getBucketItem().getDefaultStack();
    }

    private void empty() {
        boilTicks = 0;
        prevFluid = fluid;
        fluid = Fluids.EMPTY;
        changed();
    }


    public static void clientTick(World world, BlockPos pos, BlockState state, VatBlockEntity vat) {
        vat.prevHeight = vat.height;
        if (vat.fluid != Fluids.EMPTY)
            vat.height = MathHelper.lerp(0.25f,vat.height,1f);
        else
            vat.height = MathHelper.lerp(0.5f,vat.height,0f);
        if (vat.height < 1E-5)
            vat.height = 0f;

        if (vat.fluid == Fluids.EMPTY) return;

        Random random = world.getRandom();
        if (world.getTime() % 20 == 0) {
            world.addParticleClient(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                pos.getY() + random.nextDouble(),
                pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                0.0,
                0.04,
                0.0
            );
        }

        if (world.getTime() % 5 == 0 && vat.fluid == Fluids.WATER)
            world.addParticleClient(
                ParticleTypes.BUBBLE_POP,
                pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                pos.getY() + 0.8,
                pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                0,
                .05,
                0);
    }
    public static void tick(World world, BlockPos pos, BlockState state, VatBlockEntity vat) {
        if (vat.fluid == Fluids.EMPTY) return;
        ServerWorld server = (ServerWorld) world;
        Box opening =
                new Box(
                        pos.getX() + .12,
                        pos.getY() + .25,
                        pos.getZ() + .12,
                        pos.getX() + .88,
                        pos.getY() + 1,
                        pos.getZ() + .88);

        var recipes = server.getRecipeManager().getAllOfType(AlchemyRecipe.TYPE);
        for (ItemEntity entity :
                world.getEntitiesByClass(
                        ItemEntity.class, opening, e -> e.isAlive() && !e.getStack().isEmpty())) {
            // Accept only ingredients of at least one known recipe; accidental tools are safe.
            ItemStack stack = entity.getStack();
            if (recipes.stream().filter(recipe -> recipe.value().fluid() == vat.getFluid())
                    .noneMatch(r -> r.value().inputs().stream()
                            .anyMatch(input -> stack.isOf(input.item())))) continue;
            vat.add(stack.copy());
            entity.discard();
        }
        for (var entry : recipes) {
            OfferingRecipe recipe = entry.value().offering(entry.id().getValue().toString());
            if (recipe.matches(vat.offerings) && recipe.fluid() == vat.fluid) {
                recipe.consume(vat.offerings);
                InteractionEffects.magic(world, Vec3d.ofCenter(pos).add(0, .5, 0), true);
                vat.dropAll(); // Return extra ingredients rather than silently deleting them.
                vat.empty();
                ItemEntity result =
                        new ItemEntity(
                                world,
                                pos.getX() + .5,
                                pos.getY() + .65,
                                pos.getZ() + .5,
                                recipe.outputStack());
                result.setVelocity(0, .16, 0);
                world.spawnEntity(result);
                for (ItemStack byproduct : entry.value().byproducts())
                    world.spawnEntity(
                            new ItemEntity(
                                    world,
                                    pos.getX() + .5,
                                    pos.getY() + 1,
                                    pos.getZ() + .5,
                                    byproduct.copy()));
                server.spawnParticles(
                        ParticleTypes.ENCHANT,
                        pos.getX() + .5,
                        pos.getY() + 1,
                        pos.getZ() + .5,
                        25,
                        .25,
                        .25,
                        .25,
                        .1);
                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_BREWING_STAND_BREW,
                        SoundCategory.BLOCKS,
                        1,
                        1);
            }
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        boilTicks = view.getInt("BoilTicks", 0);
        fluid = Optional.of(Registries.FLUID.get(Identifier.tryParse(view.getString("Fluid","")))).orElse(Fluids.EMPTY);
        if (prevFluid == null)
            prevFluid = fluid;
        color = calculateColor();
        if (world != null && world.isClient()) world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("BoilTicks", boilTicks);
        view.putString("Fluid",Registries.FLUID.getId(fluid).toString());
    }
}
