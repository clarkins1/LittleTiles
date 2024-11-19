package team.creative.littletiles.mixin.sodium;

import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumBufferUploader;
import team.creative.littletiles.client.mod.sodium.data.BuiltSectionMeshPartsExtender;
import team.creative.littletiles.client.mod.sodium.renderer.BlockRendererExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin extends ChunkBuilderTask<ChunkBuildOutput> {
    
    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    
    @Unique
    public ChunkBuildContext buildContext;
    
    private ChunkBuilderMeshingTaskMixin(RenderSection render, int time, Vector3dc absoluteCameraPos) {
        super(render, time, absoluteCameraPos);
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "<init>(Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSection;ILorg/joml/Vector3dc;Lnet/caffeinemc/mods/sodium/client/world/cloned/ChunkRenderContext;)V")
    public void onCreated(RenderSection render, int time, Vector3dc absoluteCameraPos, ChunkRenderContext renderContext, CallbackInfo info) {
        LittleRenderPipelineType.startCompile((RenderChunkExtender) render);
    }
    
    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        this.buildContext = buildContext;
    }
    
    @Redirect(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, require = 1, at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
                    remap = true))
    public BlockEntity getBlockEntity(LevelSlice slice, BlockPos pos) {
        BlockEntity entity = slice.getBlockEntity(pos);
        if (entity instanceof BETiles be)
            LittleRenderPipelineType.compile(SectionPos.asLong(render.getChunkX(), render.getChunkY(), render.getChunkZ()), be, x -> {
                SodiumBufferUploader uploader = (SodiumBufferUploader) buildContext.buffers.get(DefaultMaterials.forRenderLayer(x));
                if (x == RenderType.translucent())
                    uploader.setTranslucentCollector(((BlockRendererExtender) buildContext.cache.getBlockRenderer()).getTranslucentCollector());
                return uploader;
            }, x -> getOrCreateBuffers(x));
        return entity;
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildEnd(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) render);
        this.buildContext = null;
        this.caches = null;
    }
    
    @Redirect(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;createMesh(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Z)Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionMeshParts;"))
    public BuiltSectionMeshParts createMesh(ChunkBuildBuffers buffers, TerrainRenderPass pass, boolean forceUnassigned) {
        BuiltSectionMeshParts parts = buffers.createMesh(pass, forceUnassigned);
        if (parts != null && caches != null)
            ((BuiltSectionMeshPartsExtender) parts).setBuffers(caches.get(((TerrainRenderPassAccessor) pass).getRenderType()));
        return parts;
    }
    
    @Unique
    public BufferCollection getOrCreateBuffers(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        BufferCollection cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new BufferCollection());
        return cache;
    }
    
}
