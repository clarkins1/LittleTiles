package team.creative.littletiles.mixin.embeddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;

@Mixin(CompactChunkVertex.class)
public interface CompactChunkVertexAccessor {
    
    @Invoker
    public static short callEncodePosition(float value) {
        throw new UnsupportedOperationException();
    }
    
}
