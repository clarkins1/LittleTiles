package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeSelection;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.item.SelectionModePacket;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.selection.SelectionMode;

public class ItemLittleBlueprint extends Item implements ILittlePlacer, IItemTooltip {
    
    public static final String CONTENT_KEY = "c";
    public static final String SELECTION_KEY = "s";
    
    public static CompoundTag getSelection(ItemStack stack) {
        return ILittleTool.getData(stack).getCompound(SELECTION_KEY);
    }
    
    public static void setSelection(ItemStack stack, CompoundTag selection) {
        var data = ILittleTool.getData(stack);
        data.put(SELECTION_KEY, selection);
        ILittleTool.setData(stack, data);
    }
    
    public static CompoundTag getContent(ItemStack stack) {
        return ILittleTool.getData(stack).getCompound(CONTENT_KEY);
    }
    
    public static void setContent(ItemStack stack, CompoundTag content) {
        var data = ILittleTool.getData(stack);
        data.put(CONTENT_KEY, content);
        ILittleTool.setData(stack, data);
    }
    
    public ItemLittleBlueprint() {
        super(new Item.Properties());
    }
    
    @Override
    public Component getName(ItemStack stack) {
        var content = getContent(stack);
        if (content.contains(LittleGroup.STRUCTURE_KEY) && content.getCompound(LittleGroup.STRUCTURE_KEY).contains("n"))
            return Component.literal(content.getCompound(LittleGroup.STRUCTURE_KEY).getString("n"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return !getContent(stack).isEmpty();
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(getContent(stack));
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return LittleGroup.loadLow(getContent(stack));
    }
    
    @Override
    public boolean shouldRenderInHand(ItemStack stack) {
        return LittleGroup.shouldRenderInHand(getContent(stack));
    }
    
    @Override
    public PlacementPreview getPlacement(Player player, Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {
        setContent(stack, LittleGroup.save(group));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        if (!((ItemLittleBlueprint) view.get().getItem()).hasTiles(view.get()))
            return new GuiRecipeSelection(view);
        return new GuiRecipe(view);
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
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level world, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockState state = world.getBlockState(result.getBlockPos());
        if (state.getBlock() instanceof BlockTile) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
            LittleTiles.NETWORK.sendToServer(new BlockPacket(world, result.getBlockPos(), player, BlockPacketAction.BLUEPRINT, nbt));
            return true;
        }
        return true;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        setSelection(stack, getSelectionMode(stack).rightClick(player, getSelection(stack), result.getBlockPos()));
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        setSelection(stack, getSelectionMode(stack).leftClick(player, getSelection(stack), result.getBlockPos()));
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(LittleGroup.printTooltip(getContent(stack)));
    }
    
    @Override
    public LittleVecGrid getCachedSize(ItemStack stack) {
        return LittleGroup.getSize(getContent(stack));
    }
    
    @Override
    public LittleVecGrid getCachedMin(ItemStack stack) {
        return LittleGroup.getMin(getContent(stack));
    }
    
    @Override
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        if (hasTiles(stack))
            return "littletiles.tiles.tooltip";
        return "littletiles.blueprint.selection.tooltip";
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        if (hasTiles(stack))
            return new Object[] { LittleTilesClient.configure.getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.mirror.getTranslatedKeyMessage() };
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(), Minecraft
                .getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack) {
        return SelectionMode.REGISTRY.get(getSelection(stack).getString("selmode"));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
        var selection = getSelection(stack);
        selection.putString("selmode", mode.getName());
        setSelection(stack, selection);
    }
}
