package team.creative.littletiles.client.mod.sodium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.DefaultChunkMeshAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.mod.sodium.SodiumInteractor;
import team.creative.littletiles.client.mod.sodium.data.LittleQuadView;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineSodium;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class SodiumBufferCache implements BufferCache {
    
    private final BufferHolder[] buffers;
    private List<TextureAtlasSprite> textures;
    private int groupCount;
    private boolean invalid;
    
    public SodiumBufferCache(BufferHolder[] buffers, List<TextureAtlasSprite> textures, int groupCount) {
        this.buffers = buffers;
        this.textures = textures;
        this.groupCount = groupCount;
    }
    
    public BufferHolder buffer(ModelQuadFacing facing) {
        return buffers[facing.ordinal()];
    }
    
    public List<TextureAtlasSprite> getUsedTextures() {
        return textures;
    }
    
    @Override
    public BufferCache extract(int index) {
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null)
                buffers[i] = this.buffers[i].extract(index);
        groupCount--;
        return new SodiumBufferCache(buffers, textures, 1);
    }
    
    @Override
    public BufferCache combine(BufferCache cache) {
        if (cache instanceof SodiumBufferCache r) {
            List<TextureAtlasSprite> sprites = new ArrayList<>();
            for (TextureAtlasSprite texture : r.getUsedTextures())
                if (!sprites.contains(texture))
                    sprites.add(texture);
            BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
            for (int i = 0; i < buffers.length; i++)
                buffers[i] = BufferHolder.combine(this.buffers[i], r.buffer(ModelQuadFacing.VALUES[i]));
            return new SodiumBufferCache(buffers, sprites, groupCount + r.groupCount());
        }
        
        if (!(cache instanceof BufferHolder))
            return null;
        
        BufferHolder[] buffers = Arrays.copyOf(this.buffers, ModelQuadFacing.COUNT);
        
        int un = ModelQuadFacing.UNASSIGNED.ordinal();
        buffers[un] = BufferHolder.combine(this.buffers[un], (BufferHolder) cache);
        
        return new SodiumBufferCache(buffers, textures, groupCount + cache.groupCount());
    }
    
    private void applySodiumOffset(ByteBuffer buffer, Vec3 vec) {
        if (buffer == null)
            return;
        
        long ptr = MemoryUtil.memAddress(buffer);
        int i = 0;
        while (i < buffer.limit()) {
            int hi = MemoryUtil.memGetInt(ptr + 0);
            int lo = MemoryUtil.memGetInt(ptr + 4);
            float x = SodiumInteractor.unpackPositionX(hi, lo) + (float) vec.x;
            float y = SodiumInteractor.unpackPositionY(hi, lo) + (float) vec.y;
            float z = SodiumInteractor.unpackPositionZ(hi, lo) + (float) vec.z;
            
            int quantX = SodiumInteractor.quantizePosition(x);
            int quantY = SodiumInteractor.quantizePosition(y);
            int quantZ = SodiumInteractor.quantizePosition(z);
            
            MemoryUtil.memPutInt(ptr + 0L, SodiumInteractor.packPositionHi(quantX, quantY, quantZ));
            MemoryUtil.memPutInt(ptr + 4L, SodiumInteractor.packPositionLo(quantX, quantY, quantZ));
            ptr += CompactChunkVertex.STRIDE;
            i += CompactChunkVertex.STRIDE;
        }
    }
    
    @Override
    public void applyOffset(Vec3 vec) {
        /* if (SodiumOptions.canUseVanillaVertices()) { TODO Check for option and iris support
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].applyOffset(vec);
            return;
        }*/
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                applySodiumOffset(buffers[i].byteBuffer(), vec);
    }
    
    @Override
    public boolean isInvalid() {
        return invalid;
    }
    
    @Override
    public void invalidate() {
        invalid = true;
    }
    
    @Override
    public boolean isAvailable() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].isAvailable())
                return false;
        return true;
    }
    
    @Override
    public int lengthToUpload() {
        int length = 0;
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && buffers[i].isAvailable())
                length += buffers[i].lengthToUpload();
        return length;
    }
    
    @Override
    public int lengthToUpload(int facing) {
        if (buffers[facing] != null && buffers[facing].isAvailable())
            return buffers[facing].lengthToUpload();
        return 0;
    }
    
    @Override
    public int groupCount() {
        return groupCount;
    }
    
    @Override
    public void eraseBuffer() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].eraseBuffer();
    }
    
    @Override
    public boolean upload(ChunkBufferUploader uploader) {
        for (TextureAtlasSprite texture : textures)
            uploader.addTexture(texture);
        
        if (uploader.hasFacingSupport()) {
            if (uploader instanceof SodiumBufferUploader u && u.isSorted()) {
                ChunkVertexType type = LittleRenderPipelineSodium.getType();
                var positionAttribute = type.getVertexFormat().getAttribute(DefaultChunkMeshAttributes.POSITION);
                boolean compact = positionAttribute.getFormat() == GlVertexAttributeFormat.UNSIGNED_INT.typeId() && positionAttribute.getCount() == 2;
                int stride = type.getVertexFormat().getStride();
                int strideRemaining = stride - (compact ? GlVertexAttributeFormat.UNSIGNED_INT.size() * 2 : GlVertexAttributeFormat.FLOAT.size() * 3);
                LittleQuadView quad = new LittleQuadView();
                
                for (int i = 0; i < buffers.length; i++) {
                    if (buffers[i] == null)
                        continue;
                    
                    if (!buffers[i].upload(i, uploader))
                        return false; // Something went wrong
                        
                    // Add translucent data by going through the buffers, collecting the quads and adding to the translucent collector
                    ModelQuadFacing facing = ModelQuadFacing.values()[i];
                    ByteBuffer buffer = buffers[i].byteBuffer();
                    var collector = u.getTranslucentCollector();
                    
                    while (buffer.hasRemaining()) {
                        quad.readVertices(buffer, compact, strideRemaining, facing);
                        collector.appendQuad(quad.getPackedNormal(), quad.getVertices(), facing);
                    }
                    buffer.rewind();
                }
            }
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].upload(i, uploader))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].upload(uploader))
                return false;
        return true;
    }
    
    @Override
    public boolean download(ChunkBufferDownloader downloader) {
        if (downloader.hasFacingSupport()) {
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].download(downloader.downloaded(i)))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].download(downloader.downloaded()))
                return false;
        return true;
    }
    
    @Override
    public void markAsAdditional() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].markAsAdditional();
    }
    
}
