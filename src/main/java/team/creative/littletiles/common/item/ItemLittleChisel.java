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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiChisel;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket.VanillaBlockAction;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleChisel extends Item implements ILittlePlacer, IItemTooltip {
    
    public static LittleShape getShape(ItemStack stack) {
        return getShape(ILittleTool.getData(stack));
    }
    
    public static LittleShape getShape(CompoundTag nbt) {
        return ShapeRegistry.REGISTRY.get(nbt.getString("shape"));
    }
    
    public static void setShape(ItemStack stack, LittleShape shape) {
        var data = ILittleTool.getData(stack);
        setShape(data, shape);
        ILittleTool.setData(stack, data);
    }
    
    public static void setShape(CompoundTag nbt, LittleShape shape) {
        nbt.putString("shape", shape.getKey());
    }
    
    public static LittleElement getElement(ItemStack stack) {
        var data = ILittleTool.getData(stack);
        if (data.contains("element"))
            return new LittleElement(data.getCompound("element"));
        
        LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
        setElement(stack, element);
        return element;
    }
    
    public static LittleElement getElement(CompoundTag nbt) {
        if (nbt.contains("element"))
            return new LittleElement(nbt.getCompound("element"));
        
        return new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE);
    }
    
    public static void setElement(ItemStack stack, LittleElement element) {
        var data = ILittleTool.getData(stack);
        data.put("element", element.save(new CompoundTag()));
        ILittleTool.setData(stack, data);
    }
    
    public static void setElement(CompoundTag nbt, LittleElement element) {
        nbt.put("element", element.save(new CompoundTag()));
    }
    
    public static ShapeSelection selection;
    
    public ItemLittleChisel() {
        super(new Item.Properties().stacksTo(1));
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
        LittleShape shape = getShape(stack);
        tooltip.add(Component.translatable("gui.shape").append(": ").append(Component.translatable(shape.getTranslatableName())));
        shape.addExtraInformation(ILittleTool.getData(stack), tooltip);
        tooltip.add(Component.literal(TooltipUtils.printColor(getElement(stack).color)));
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return null;
    }
    
    @Override
    public boolean shouldRenderInHand(ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return null;
    }
    
    @Override
    public PlacementPreview getPlacement(Player player, Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        var sel = selection;
        if (sel != null) {
            LittleBoxes boxes = sel.getBoxes(allowLowResolution, getPositionGrid(player, stack));
            LittleGroupAbsolute previews = new LittleGroupAbsolute(boxes.pos);
            previews.add(boxes.grid, getElement(stack), boxes);
            return PlacementPreview.absolute(level, stack, previews, sel.getFirst().pos.facing);
        }
        return null;
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {}
    
    protected ShapeSelection createSelection(ItemStack stack) {
        return new ShapeSelection(stack, getPlacementMode(stack).placeInside);
    }
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        if (!client)
            return;
        if (selection != null)
            selection.rotate(player, stack, rotation);
        else
            createSelection(stack).rotate(player, stack, rotation);
    }
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        if (!client)
            return;
        if (selection != null)
            selection.mirror(player, stack, axis);
        else
            createSelection(stack).mirror(player, stack, axis);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getPreviewAlphaFactor() {
        return 0.4F;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void tick(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection == null)
            selection = createSelection(stack);
        selection.setLast(player, stack, position, result);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldCache() {
        return false;
    }
    
    @Override
    public void configured(ItemStack stack, CompoundTag nbt) {
        ILittlePlacer.super.configured(stack, nbt);
        if (selection.countPositions() <= 1)
            selection = null;
    }
    
    @Override
    public void onDeselect(Level level, ItemStack stack, Player player) {
        selection = null;
    }
    
    @Override
    public void onClickAir(Player player, ItemStack stack) {
        if (selection != null)
            selection.click(player);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection != null)
            selection.click(player);
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            selection = null;
            LittleTilesClient.PREVIEW_RENDERER.removeMarked();
        } else if (selection != null)
            return selection.addAndCheckIfPlace(player, position, result);
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockState state = level.getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            LittleTiles.NETWORK.sendToServer(new VanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.CHISEL));
            return true;
        } else if (state.getBlock() instanceof BlockTile) {
            LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), player, BlockPacketAction.CHISEL, new CompoundTag()));
            return true;
        }
        return false;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiChisel(view);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public IMarkMode onMark(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getTranslatable(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.mark
                .getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
}
