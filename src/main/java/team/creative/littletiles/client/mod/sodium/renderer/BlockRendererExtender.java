package team.creative.littletiles.client.mod.sodium.renderer;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;

public interface BlockRendererExtender {
    
    public ColorProviderRegistry colorRegistry();
    
    public MutableQuadViewImpl getEditorQuadAndClear();
    
    public void callBufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material);
}
