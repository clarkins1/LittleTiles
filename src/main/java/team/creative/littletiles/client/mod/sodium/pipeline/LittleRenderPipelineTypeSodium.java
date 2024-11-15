package team.creative.littletiles.client.mod.sodium.pipeline;

import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class LittleRenderPipelineTypeSodium extends LittleRenderPipelineType<LittleRenderPipelineSodium> {
    
    public LittleRenderPipelineTypeSodium() {
        super(LittleRenderPipelineSodium::new);
    }
    
}
