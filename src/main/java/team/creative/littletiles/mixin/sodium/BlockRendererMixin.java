package team.creative.littletiles.mixin.sodium;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.mod.sodium.renderer.BlockRendererExtender;

@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin implements BlockRendererExtender {
    
    @Unique
    private boolean takenOver;
    
    @Shadow
    @Final
    private Vector3f posOffset;
    
    @Shadow
    @Final
    private ColorProviderRegistry colorProviderRegistry;
    
    @Shadow
    private TranslucentGeometryCollector collector;
    
    @Override
    public void setOffset(float x, float y, float z) {
        posOffset.x = x;
        posOffset.y = y;
        posOffset.z = z;
    }
    
    @Override
    public void markAsTakenOver() {
        takenOver = true;
    }
    
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
    
    @Redirect(
            method = "bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;addSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"),
            require = 1)
    public void redirectAddSprite(ChunkModelBuilder builder, TextureAtlasSprite sprite) {
        if (takenOver)
            return;
        builder.addSprite(sprite);
    }
    
    @Override
    public TranslucentGeometryCollector getTranslucentCollector() {
        return collector;
    }
    
}
