package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

@Mixin(targets = "net/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegionManager$PendingSectionMeshUpload")
public interface PendingSectionMeshUploadAccessor {
    
    @Accessor(remap = false)
    public RenderSection getSection();
    
    @Accessor(remap = false)
    public BuiltSectionMeshParts getMeshData();
    
    @Accessor(remap = false)
    public TerrainRenderPass getPass();
    
}
