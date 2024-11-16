package team.creative.littletiles.mixin.sodium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;

@Mixin(ChunkMeshBufferBuilder.class)
public interface ChunkMeshBufferBuilderAccessor {
    
    @Accessor(remap = false)
    public int getStride();
    
    @Accessor(remap = false)
    public ByteBuffer getBuffer();
    
    @Accessor(remap = false)
    public int getVertexCount();
    
    @Accessor(remap = false)
    public void setVertexCount(int count);
    
    @Accessor(remap = false)
    public int getVertexCapacity();
    
    @Invoker(remap = false)
    public void callGrow(int len);
    
    @Accessor(remap = false)
    public ChunkVertexEncoder getEncoder();
    
}
