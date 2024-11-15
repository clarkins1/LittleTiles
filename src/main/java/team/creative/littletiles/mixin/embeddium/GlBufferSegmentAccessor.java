package team.creative.littletiles.mixin.embeddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferArena;
import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferSegment;

@Mixin(GlBufferSegment.class)
public interface GlBufferSegmentAccessor {
    
    @Accessor(remap = false)
    public GlBufferArena getArena();
}
