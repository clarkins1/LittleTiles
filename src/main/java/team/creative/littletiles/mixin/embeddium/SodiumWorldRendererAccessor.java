package team.creative.littletiles.mixin.embeddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;

@Mixin(SodiumWorldRenderer.class)
public interface SodiumWorldRendererAccessor {
    
    @Accessor(remap = false)
    public RenderSectionManager getRenderSectionManager();
    
}
