package team.creative.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;

public class AnimationPreview {
    
    public final LittleAnimationEntity animation;
    public final LittleGroup previews;
    public final LittleBox entireBox;
    public final LittleGrid grid;
    public final AABB box;
    
    public AnimationPreview(LittleLevel fakeLevel, LittleStructure structure, LittleGroup previews, HolderLookup.Provider provider) throws LittleActionException {
        this.previews = previews;
        this.grid = previews.getGrid();
        BlockPos pos = new BlockPos(0, 0, 0);
        
        fakeLevel.setOrigin(new Vec3d());
        LittleAnimationLevel subLevel = new LittleAnimationLevel((Level) fakeLevel);
        
        if (!previews.hasStructure()) {
            CompoundTag nbt = new CompoundTag();
            new LittleFixedStructure(LittleStructureRegistry.REGISTRY.get("fixed"), null).save(nbt, provider);
            List<LittleGroup> newChildren = new ArrayList<>();
            for (LittleGroup group : previews.children.children())
                newChildren.add(group.copy());
            LittleGroup group = new LittleGroup(nbt, newChildren);
            final var oldPreviews = previews;
            group.addAll(grid, new FunctionIterator<>(oldPreviews, x -> x.copy()));
            previews = group;
        }
        entireBox = previews.getSurroundingBox();
        box = entireBox.getBB(grid);
        StructureAbsolute absolute = structure != null ? structure.createAnimationCenter(pos, grid) : null;
        if (absolute == null)
            absolute = new StructureAbsolute(pos, entireBox, previews.getGrid());
        Placement placement = new Placement(null, subLevel, PlacementPreview.load((UUID) null, PlacementMode.ALL, new LittleGroupAbsolute(pos, previews), Facing.EAST));
        
        animation = new LittleAnimationEntity((Level) fakeLevel, subLevel, absolute, placement);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void setupRendering(PoseStack pose) {
        animation.getOrigin().setupRendering(pose, 0, 0, 0, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }
    
    public void unload() {
        animation.destroyAnimation();
    }
    
    public void set(PhysicalState state) {
        animation.physic.set(state);
    }
    
    public void tick() {
        animation.getOrigin().tick();
    }
    
    public void setCenter(StructureAbsolute center) {
        animation.setCenter(center);
    }
}
