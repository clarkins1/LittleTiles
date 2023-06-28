package team.creative.littletiles.common.block.mc;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.api.common.block.ILittleMCBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;

public class BlockWater extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public BlockWater(Properties properties) {
        super(properties);
    }
    
    @Override
    public Block asBlock() {
        return this;
    }
    
    @Override
    public boolean isFluid(TagKey<Fluid> fluid) {
        return fluid.equals(FluidTags.LAVA);
    }
    
    @Override
    public boolean isLiquid() {
        return true;
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result) {
        if (player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingWater) {
            if (this == LittleTilesRegistry.WATER.get())
                tile.setState(LittleTilesRegistry.FLOWING_WATER.get().defaultBlockState());
            else
                tile.setState(LittleTilesRegistry.WHITE_FLOWING_WATER.get().defaultBlockState());
            parent.getBE().updateTiles();
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, tile, box, player, result);
    }
    
    @Override
    public BlockState getFakeState(BlockState state) {
        return Blocks.WATER.defaultBlockState();
    }
    
}
