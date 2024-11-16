package team.creative.littletiles.mixin.sodium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

@Mixin(ChunkModelBuilder.class)
public interface ChunkModelBuilderMixin extends ChunkBufferUploader {
    
    @Override
    public default int uploadIndex() {
        return uploadIndex(ModelQuadFacing.UNASSIGNED.ordinal());
    }
    
    @Override
    public default void upload(ByteBuffer buffer) {
        upload(ModelQuadFacing.UNASSIGNED.ordinal(), buffer);
    }
    
    @Override
    public default boolean hasFacingSupport() {
        return true;
    }
    
    @Override
    public default int uploadIndex(int facing) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        return vertex.getVertexCount() * vertex.getStride();
    }
    
    @Override
    public default void upload(int facing, ByteBuffer buffer) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        
        // Add to vertex buffer
        int vertexStart = vertex.getVertexCount();
        int vertexCount = buffer.limit() / vertex.getStride();
        if (vertexStart + vertexCount >= vertex.getVertexCapacity())
            vertex.callGrow(vertex.getStride() * vertexCount);
        ByteBuffer data = vertex.getBuffer();
        int index = vertex.getVertexCount() * vertex.getStride();
        data.position(index);
        data.put(buffer);
        buffer.rewind();
        data.rewind();
        vertex.setVertexCount(vertex.getVertexCount() + vertexCount);
    }
    
    @Override
    public default void addSprite(TextureAtlasSprite texture) {
        ((ChunkModelBuilder) this).addSprite(texture);
    }
    
}
