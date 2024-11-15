package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;

@Mixin(BakedChunkModelBuilder.class)
public interface BakedChunkModelBuilderAccessor {
    
    @Accessor(remap = false)
    public boolean getSplitBySide();
    
}
