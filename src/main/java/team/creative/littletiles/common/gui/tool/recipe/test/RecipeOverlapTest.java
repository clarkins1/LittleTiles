package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.vec.LittleVec;

public class RecipeOverlapTest extends RecipeTestModule {
    
    private HashMap<BlockPos, RecipeOverlayTestBlock> blocks;
    private HashMap<GuiTreeItemStructure, LittleBoxesNoOverlap> overlapped;
    
    @Override
    public void startTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = new HashMap<>();
    }
    
    private RecipeOverlayTestBlock getOrCreate(BlockPos pos) {
        RecipeOverlayTestBlock block = blocks.get(pos);
        if (block == null)
            blocks.put(pos.immutable(), block = new RecipeOverlayTestBlock());
        return block;
    }
    
    private void addOverlay(GuiTreeItemStructure other, BlockPos pos, LittleGrid grid, LittleBox box) {
        if (overlapped == null)
            overlapped = new HashMap<>();
        
        LittleBoxesNoOverlap boxes = overlapped.get(other);
        if (boxes == null)
            overlapped.put(other, boxes = new LittleBoxesNoOverlap(BlockPos.ZERO, grid));
        boxes.addBox(grid, pos, box);
    }
    
    @Override
    public void test(GuiTreeItemStructure item, RecipeTestResults results) {
        overlapped = null;
        
        MutableBlockPos pos = new MutableBlockPos();
        LittleGrid grid = item.group.getGrid();
        for (LittleTile tile : item.group)
            for (LittleBox box : tile)
                box.splitIterator(grid, pos, LittleVec.ZERO, (x, y) -> getOrCreate(x).add(item, x, grid, y));
            
        if (overlapped != null) {
            for (Entry<GuiTreeItemStructure, LittleBoxesNoOverlap> entry : overlapped.entrySet()) {
                if (entry.getKey() == item)
                    results.reportError(new SelfOverlapError(item, entry.getValue()));
                else
                    results.reportError(new OverlapError(item, entry.getKey(), entry.getValue()));
            }
        }
        
        overlapped = null;
        
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = null;
    }
    
    public class RecipeOverlayTestBlock implements IGridBased {
        
        public HashMap<GuiTreeItemStructure, List<LittleBox>> structureBoxes = new HashMap<>();
        private LittleGrid grid = LittleGrid.min();
        
        public void add(GuiTreeItemStructure item, BlockPos pos, LittleGrid grid, LittleBox box) {
            if (grid.count > this.grid.count)
                convertTo(grid);
            else if (grid.count < this.grid.count)
                box.convertTo(grid, this.grid);
            
            for (Entry<GuiTreeItemStructure, List<LittleBox>> entry : structureBoxes.entrySet()) {
                for (LittleBox other : entry.getValue()) {
                    if (LittleBox.intersectsWith(box, other)) {
                        LittleBox intersecting = box.intersection(other);
                        addOverlay(entry.getKey(), pos, grid, intersecting);
                    }
                }
            }
            
            List<LittleBox> boxes = structureBoxes.get(item);
            if (boxes == null)
                structureBoxes.put(item, boxes = new ArrayList<>());
            boxes.add(box);
        }
        
        @Override
        public LittleGrid getGrid() {
            return grid;
        }
        
        @Override
        public void convertTo(LittleGrid to) {
            for (List<LittleBox> boxes : structureBoxes.values())
                for (LittleBox box : boxes)
                    box.convertTo(grid, to);
            this.grid = to;
        }
        
        @Override
        public int getSmallest() {
            int smallest = 0;
            for (List<LittleBox> boxes : structureBoxes.values())
                for (LittleBox box : boxes)
                    smallest = Math.max(smallest, box.getSmallest(grid));
            return smallest;
        }
        
    }
    
    public static class SelfOverlapError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final LittleBoxesNoOverlap boxes;
        
        public SelfOverlapError(GuiTreeItemStructure structure, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.boxes = boxes;
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.overlap.self.title", structure.getTitle());
        }
        
        @Override
        public Component description() {
            int volume = boxes.littleVolume();
            if (volume >= boxes.grid.count)
                return GuiControl.translatable("gui.recipe.test.overlap.desc.large", TooltipUtils.print(boxes.grid.pixelVolume * volume));
            return GuiControl.translatable("gui.recipe.test.overlap.desc.small", TooltipUtils.print(volume), boxes.grid);
        }
        
        @Override
        public Component tooltip(GuiTreeItemStructure structure) {
            return header();
        }
        
        @Override
        public Iterator<GuiTreeItemStructure> iterator() {
            return new SingleIterator<>(structure);
        }
        
        @Override
        public void create(GuiRecipe recipe, GuiParent parent) {
            parent.add(new GuiButton("fix", x -> {}).setTranslate("gui.recipe.test.overlap.fix"));
        }
    }
    
    public static class OverlapError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final GuiTreeItemStructure structure2;
        private final LittleBoxesNoOverlap boxes;
        
        public OverlapError(GuiTreeItemStructure structure, GuiTreeItemStructure structure2, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.structure2 = structure2;
            this.boxes = boxes;
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.overlap.title", structure.getTitle(), structure2.getTitle());
        }
        
        @Override
        public Component description() {
            int volume = boxes.littleVolume();
            if (volume >= boxes.grid.count)
                return GuiControl.translatable("gui.recipe.test.overlap.desc.large", TooltipUtils.print(boxes.grid.pixelVolume * volume));
            return GuiControl.translatable("gui.recipe.test.overlap.desc.small", TooltipUtils.print(volume), boxes.grid);
        }
        
        @Override
        public Iterator<GuiTreeItemStructure> iterator() {
            return new ArrayIterator<>(structure, structure2);
        }
        
        @Override
        public Component tooltip(GuiTreeItemStructure structure) {
            return GuiControl.translatable("gui.recipe.test.overlap.tooltip", structure == this.structure ? structure2.getTitle() : this.structure.getTitle());
        }
        
        @Override
        public void create(GuiRecipe recipe, GuiParent parent) {
            parent.add(new GuiLabel("remove").setTranslate("gui.recipe.test.overlap.remove"));;
            parent.add(new GuiButton("remove", x -> {}).setTitle(Component.literal(this.structure.getTitle())));
            parent.add(new GuiButton("remove2", x -> {}).setTitle(Component.literal(this.structure2.getTitle())));
            parent.add(new GuiButton("move", x -> {}).setTranslate("gui.recipe.test.overlap.move"));
        }
        
    }
    
}
