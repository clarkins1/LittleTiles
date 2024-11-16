package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.renderer.RenderType;

@Mixin(TerrainRenderPass.class)
public interface TerrainRenderPassAccessor {
    
    @Accessor(remap = false)
    public RenderType getRenderType();
    
}
