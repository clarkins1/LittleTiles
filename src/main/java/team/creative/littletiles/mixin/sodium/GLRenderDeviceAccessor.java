package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;

@Mixin(GLRenderDevice.class)
public interface GLRenderDeviceAccessor {
    
    @Accessor(remap = false)
    public boolean getIsActive();
    
}
