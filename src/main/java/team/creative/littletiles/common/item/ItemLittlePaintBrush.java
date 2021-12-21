package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.SubGuiColorTube;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.GuiGridSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittlePaintBrush extends Item implements ILittleEditor, IItemTooltip {
    
    public static ShapeSelection selection;
    
    public ItemLittlePaintBrush() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    public static int getColor(ItemStack stack) {
        if (stack == null)
            return ColorUtils.WHITE;
        if (!stack.hasTag())
            stack.setTag(new CompoundTag());
        if (!stack.getTag().contains("color"))
            setColor(stack, ColorUtils.WHITE);
        return stack.getTag().getInt("color");
    }
    
    public static void setColor(ItemStack stack, int color) {
        if (stack == null)
            return;
        if (!stack.hasTag())
            stack.setTag(new CompoundTag());
        stack.getTag().putInt("color", color);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LittleShape shape = getShape(stack);
        tooltip.add(new TranslatableComponent("gui.shape").append(": ").append(new TranslatableComponent(shape.getKey())));
        shape.addExtraInformation(stack.getTag(), tooltip);
        tooltip.add(new TextComponent(TooltipUtils.printColor(getColor(stack))));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
        if (!level.isClientSide)
            GuiHandler.openItemGui(player, hand);
        return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        return new SubGuiColorTube(stack);
    }
    
    public static LittleShape getShape(ItemStack stack) {
        String shape = stack.getOrCreateTag().getString("shape");
        if (shape.equals("tile") || shape.equals(""))
            return ShapeRegistry.tileShape;
        return ShapeRegistry.getShape(shape);
    }
    
    @Override
    public void onDeselect(Level level, ItemStack stack, Player player) {
        if (selection != null)
            selection = null;
    }
    
    @Override
    public boolean hasCustomBoxes(Level level, ItemStack stack, Player player, BlockState state, PlacementPosition pos, BlockHitResult result) {
        return LittleAction.isBlockValid(state) || level.getBlockEntity(result.getBlockPos()) instanceof BETiles;
    }
    
    @Override
    public LittleBoxes getBoxes(Level level, ItemStack stack, Player player, PlacementPosition pos, BlockHitResult result) {
        if (selection == null)
            selection = new ShapeSelection(stack, true);
        selection.setLast(player, stack, pos, result);
        return selection.getBoxes(true);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            if (selection.addAndCheckIfPlace(player, position, result)) {
                if (ItemLittleHammer.isFiltered())
                    new LittleActionColorBoxesFiltered(selection.getBoxes(false), getColor(stack), false, ItemLittleHammer.getFilter()).execute();
                else
                    new LittleActionColorBoxes(selection.getBoxes(false), getColor(stack), false).execute();
                selection = null;
            }
        return false;
    }
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        if (client && selection != null)
            selection.rotate(player, stack, rotation);
        else
            new ShapeSelection(stack, false).rotate(player, stack, rotation);
    }
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        if (client && selection != null)
            selection.mirror(player, stack, axis);
        else
            new ShapeSelection(stack, false).mirror(player, stack, axis);
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public IMarkMode onMark(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(result.getBlockPos());
        if (blockEntity instanceof BETiles)
            LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.COLOR_TUBE, new CompoundTag()));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
        return new GuiGridSelector(stack, ItemMultiTiles.currentContext, ItemLittleHammer.isFiltered(), ItemLittleHammer.getFilter()) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, boolean activeFilter, BiFilter<IParentCollection, LittleTile> filter) {
                ItemLittleHammer.setFilter(activeFilter, selector);
                if (selection != null)
                    selection.convertTo(grid);
                ItemMultiTiles.currentContext = grid;
                return nbt;
            }
        };
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.mark
                .getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage(), LittleTilesClient.configureAdvanced.getTranslatedKeyMessage() };
    }
}
