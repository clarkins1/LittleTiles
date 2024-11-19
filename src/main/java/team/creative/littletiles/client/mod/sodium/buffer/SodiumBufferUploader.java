package team.creative.littletiles.client.mod.sodium.buffer;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public interface SodiumBufferUploader extends ChunkBufferUploader {
    
    public boolean isSorted();
    
    public TranslucentGeometryCollector getTranslucentCollector();
    
    public void setTranslucentCollector(TranslucentGeometryCollector collector);
}
