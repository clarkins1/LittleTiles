package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import team.creative.littletiles.client.mod.sodium.renderer.BlockRendererExtender;

@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin implements BlockRendererExtender {
    
    @Shadow
    @Final
    private ColorProviderRegistry colorProviderRegistry;
    
    @Override
    public MutableQuadViewImpl getEditorQuadAndClear() {
        return (MutableQuadViewImpl) ((AbstractBlockRenderContext) (Object) this).getEmitter();
    }
    
    @Override
    public ColorProviderRegistry colorRegistry() {
        return colorProviderRegistry;
    }
    
    @Shadow
    protected abstract void bufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material);
    
    @Override
    public void callBufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material) {
        bufferQuad(quad, brightnesses, material);
    }
}
