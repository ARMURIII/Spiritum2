package net.dark.spiritum.block;

import com.mojang.serialization.MapCodec;

import net.dark.spiritum.block.entity.VatBlockEntity;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;

public class AlchemyVatBlock extends BlockWithEntity {
    public static final MapCodec<AlchemyVatBlock> CODEC = createCodec(AlchemyVatBlock::new);

    public AlchemyVatBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VatBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient()
                ? validateTicker(type, ModContent.VAT_ENTITY, VatBlockEntity::clientTick)
                : validateTicker(type, ModContent.VAT_ENTITY, VatBlockEntity::tick);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                Block.createCuboidShape(0, 0, 0, 3, 6, 3),
                Block.createCuboidShape(13, 0, 0, 16, 6, 3),
                Block.createCuboidShape(0, 0, 13, 3, 6, 16),
                Block.createCuboidShape(13, 0, 13, 16, 6, 16),
                Block.createCuboidShape(0, 6, 0, 16, 7, 16),
                Block.createCuboidShape(0, 7, 0, 2, 16, 16),
                Block.createCuboidShape(14, 7, 0, 16, 16, 16),
                Block.createCuboidShape(2, 7, 0, 14, 16, 2),
                Block.createCuboidShape(2, 7, 14, 14, 16, 16));
    }

    @Override
    protected ActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit) {
        if (!(stack.getItem() instanceof BucketItem)) return ActionResult.PASS;
        if (world.getBlockEntity(pos) instanceof VatBlockEntity vat) {
            if (stack.isOf(Items.BUCKET) && vat.getFluid() != Fluids.EMPTY)
                player.setStackInHand(
                    hand, ItemUsage.exchangeStack(stack, player,vat.empty(stack)));
            else if (vat.getFluid() == Fluids.EMPTY) {
                vat.fill(stack);
                player.setStackInHand(
                    hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            }
        }
        return ActionResult.SUCCESS;
    }
}
