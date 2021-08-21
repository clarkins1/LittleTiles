package team.creative.littletiles.common.structure.registry;

import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.group.LittleGroup;

public class StructureIngredientRule implements IStructureIngredientRule {
    
    public static final StructureIngredientScaler SINGLE = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittleGroup previews) {
            return 1;
        }
    };
    
    public static final StructureIngredientScaler LONGEST_SIDE = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittleGroup group) {
            LittleVec vec = group.getSize();
            int side = vec.x;
            if (side < vec.y)
                side = vec.y;
            if (side < vec.z)
                side = vec.z;
            return group.getGrid().toVanillaGrid(side);
        }
    };
    
    public static final StructureIngredientScaler VOLUME = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittleGroup previews) {
            return previews.getVolume();
        }
    };
    
    public final StructureIngredientScaler scale;
    public final LittleIngredient ingredient;
    
    public StructureIngredientRule(StructureIngredientScaler scale, LittleIngredient ingredient) {
        this.scale = scale;
        this.ingredient = ingredient;
    }
    
    @Override
    public void add(LittleGroup group, LittleIngredients ingredients) {
        double volume = scale.calculate(group);
        if (volume > 0) {
            LittleIngredient toAdd = ingredient.copy();
            toAdd.scaleAdvanced(volume);
            ingredients.add(toAdd);
        }
    }
    
    public static abstract class StructureIngredientScaler {
        
        public abstract double calculate(LittleGroup group);
        
    }
    
    public static class StructureIngredientScalerVolume extends StructureIngredientScaler {
        
        public final double scale;
        
        public StructureIngredientScalerVolume(double scale) {
            this.scale = scale;
        }
        
        @Override
        public double calculate(LittleGroup group) {
            return VOLUME.calculate(group) * scale;
        }
        
    }
    
}
