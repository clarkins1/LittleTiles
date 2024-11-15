package team.creative.littletiles.client.mod.embeddium.pipeline;

import net.caffeinemc.mods.sodium.client.model.light.data.HashLightDataCache;
import net.minecraft.world.level.BlockAndTintGetter;

public class LittleLightDataAccess extends HashLightDataCache {
    
    public LittleLightDataAccess() {
        super(null);
    }
    
    public void prepare(BlockAndTintGetter level) {
        this.level = level;
        clearCache();
    }
    
}
