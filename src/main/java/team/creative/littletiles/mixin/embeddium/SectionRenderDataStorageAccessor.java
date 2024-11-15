package team.creative.littletiles.mixin.embeddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferSegment;
import net.caffeinemc.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;

@Mixin(SectionRenderDataStorage.class)
public interface SectionRenderDataStorageAccessor {
    
    @Accessor(remap = false)
    public GlBufferSegment[] getAllocations();
}
