package team.creative.littletiles.mixin.embeddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;

@Mixin(ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    
    @Accessor(remap = false)
    public ChunkBuildContext getLocalContext();
}
