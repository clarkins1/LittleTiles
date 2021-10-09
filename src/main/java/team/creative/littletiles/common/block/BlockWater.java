package team.creative.littletiles.common.block;

import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent.LittleDyeableTransparent;
import com.creativemd.littletiles.common.block.EntityPlayer;
import com.creativemd.littletiles.common.block.EnumFacing;
import com.creativemd.littletiles.common.block.EnumHand;
import com.creativemd.littletiles.common.block.IBlockState;
import com.creativemd.littletiles.common.block.IParentTileList;
import com.creativemd.littletiles.common.block.ItemBucket;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.block.ILittleBlock;
import team.creative.littletiles.common.tile.LittleTile;

public class BlockWater extends Block implements ILittleBlock {
    
    public BlockWater(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean canWalkThrough() {
        return true;
    }
    
    @Override
    public boolean isMaterial(Material material) {
        return material == Material.WATER;
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
    public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState state = tile.getBlockState();
        if (state.getValue(VARIANT).isWater() && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket) {
            if (state.getValue(VARIANT) == LittleDyeableTransparent.WATER)
                tile.setBlock(LittleTiles.flowingWater, 0);
            else
                tile.setBlock(LittleTiles.whiteFlowingWater, 0);
            parent.getTe().updateTiles();
            return true;
        }
        return ISpecialBlockHandler.super.onBlockActivated(parent, tile, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }
    
}
