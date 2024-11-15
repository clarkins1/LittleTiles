package team.creative.littletiles.client.mod.sodium.level;

import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;

public interface LittleLevelSliceExtender {
    
    public void setLevel(Level level);
    
    public static LevelSlice create() {
        return CreativeHackery.allocateInstance(LevelSlice.class);
    }
    
}
