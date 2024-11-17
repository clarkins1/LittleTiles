package team.creative.littletiles.client.mod.sodium.renderer;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.minecraft.core.BlockPos;

public interface BlockRendererExtender {
    
    public default void setOffset(BlockPos pos) {
        setOffset(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public void setOffset(float x, float y, float z);
    
    public void markAsTakenOver();
    
    public ColorProviderRegistry colorRegistry();
    
    public MutableQuadViewImpl getEditorQuadAndClear();
    
    public void callBufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material);
}
