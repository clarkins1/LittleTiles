package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiScrewdriver;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.packet.item.ScrewdriverSelectionPacket;
import team.creative.littletiles.common.placement.PlacementPosition;

public class ItemLittleScrewdriver extends Item implements ILittleTool, IItemTooltip {
    
    public ItemLittleScrewdriver() {
        super(new Item.Properties().stacksTo(1));
    }
    
    public void onClick(Player player, boolean rightClick, BlockPos pos, ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (rightClick) {
            data.putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.level().isClientSide)
                player.sendSystemMessage(Component.translatable("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            data.putIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.level().isClientSide)
                player.sendSystemMessage(Component.translatable("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
        }
        ILittleTool.setData(stack, data);
    }
    
    @Override
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        onClick(player, true, result.getBlockPos(), stack);
        LittleTiles.NETWORK.sendToServer(new ScrewdriverSelectionPacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        onClick(player, false, result.getBlockPos(), stack);
        LittleTiles.NETWORK.sendToServer(new ScrewdriverSelectionPacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiScrewdriver(view);
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var data = ILittleTool.getData(stack);
        if (data.contains("pos1")) {
            int[] array = data.getIntArray("pos1");
            if (array.length == 3)
                tooltip.add(Component.literal("1: " + array[0] + " " + array[1] + " " + array[2]));
        } else
            tooltip.add(Component.literal("1: ").append(Component.translatable("gui.click.left")));
        
        if (data.contains("pos2")) {
            int[] array = data.getIntArray("pos2");
            if (array.length == 3)
                tooltip.add(Component.literal("2: " + array[0] + " " + array[1] + " " + array[2]));
        } else
            tooltip.add(Component.literal("2: ").append(Component.translatable("gui.click.right")));
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    @Override
    public LittleGrid getPositionGrid(Player player, ItemStack stack) {
        return LittleGrid.overallDefault();
    }
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {}
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {}
    
}
