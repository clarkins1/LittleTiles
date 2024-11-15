package team.creative.littletiles.client.mod.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.LocalSectionIndex;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.entity.LittleAnimationRenderManagerSodium;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineSodium;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineTypeSodium;
import team.creative.littletiles.client.mod.sodium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;

public class SodiumInteractor {
    
    public static final LittleRenderPipelineTypeSodium PIPELINE = new LittleRenderPipelineTypeSodium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Sodium extension");
        SodiumManager.RENDERING_LEVEL = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return LittleRenderPipelineSodium.getChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & (RenderRegion.REGION_WIDTH - 1);
                int rY = SectionPos.y(pos) & (RenderRegion.REGION_HEIGHT - 1);
                int rZ = SectionPos.z(pos) & (RenderRegion.REGION_LENGTH - 1);
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(Level level, SectionPos pos) {
                return pos.origin();
            }
        };
        SodiumManager.RENDERING_ANIMATION = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public void prepareModelOffset(Level level, MutableBlockPos modelOffset, BlockPos pos) {
                BlockPos chunkOffset = ((LittleAnimationEntity) ((LittleLevel) level).getHolder()).getCenter().chunkOrigin;
                modelOffset.set(pos.getX() - chunkOffset.getX(), pos.getY() - chunkOffset.getY(), pos.getZ() - chunkOffset.getZ());
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return ((LittleLevel) level).getRenderManager().getRenderChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & DefaultChunkRendererExtender.REGION_WIDTH_M;
                int rY = SectionPos.y(pos) & DefaultChunkRendererExtender.REGION_HEIGHT_M;
                int rZ = SectionPos.z(pos) & DefaultChunkRendererExtender.REGION_LENGTH_M;
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(Level level, SectionPos pos) {
                return ((LittleAnimationEntity) ((LittleLevel) level).getHolder()).getCenter().chunkOrigin;
            }
            
            @Override
            public long prepareQueue(long pos) {
                return 0;
            }
        };
    }
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        return new LittleAnimationRenderManagerSodium(entity);
    }
    
}
