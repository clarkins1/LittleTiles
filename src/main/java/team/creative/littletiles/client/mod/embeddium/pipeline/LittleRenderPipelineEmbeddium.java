package team.creative.littletiles.client.mod.embeddium.pipeline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.embeddium.EmbeddiumInteractor;
import team.creative.littletiles.client.mod.embeddium.buffer.EmbeddiumBufferCache;
import team.creative.littletiles.client.mod.embeddium.level.LittleLevelSliceExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.embeddium.BlockRenderContextAccessor;
import team.creative.littletiles.mixin.embeddium.BlockRendererAccessor;
import team.creative.littletiles.mixin.embeddium.ChunkBuildBuffersAccessor;
import team.creative.littletiles.mixin.embeddium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.embeddium.ChunkMeshBufferBuilderAccessor;
import team.creative.littletiles.mixin.embeddium.RenderSectionManagerAccessor;
import team.creative.littletiles.mixin.embeddium.SodiumWorldRendererAccessor;

public class LittleRenderPipelineEmbeddium extends LittleRenderPipeline {
    
    public static RenderChunkExtender getChunk(long pos) {
        return (RenderChunkExtender) ((RenderSectionManagerAccessor) ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager()).callGetRenderSection(
            SectionPos.x(pos), SectionPos.y(pos), SectionPos.z(pos));
    }
    
    public static ChunkVertexType getType() {
        return RenderingThread.getOrCreate(EmbeddiumInteractor.PIPELINE).type;
    }
    
    private ChunkBuildBuffers buildBuffers;
    private ChunkVertexType type;
    private final LevelSlice slice = LittleLevelSliceExtender.create();
    private BlockRenderer renderer;
    private LittleLightDataAccess lightAccess;
    private LightPipelineProvider lighters;
    private QuadLightData cachedQuadLightData = new QuadLightData();
    public BlockRenderContext context = new BlockRenderContext(slice, null);
    private Set<TextureAtlasSprite> sprites = new ObjectOpenHashSet<>();
    private MutableBlockPos modelOffset = new MutableBlockPos();
    private IntArrayList[] indexes;
    private MutableBlockPos scratchColorPos = new MutableBlockPos();
    private int[] faceCounters = new int[ModelQuadFacing.COUNT];
    private int[] colors = new int[4];
    private Vec3d cubeCenter = new Vec3d();
    
    public LittleRenderPipelineEmbeddium() {
        indexes = new IntArrayList[ModelQuadFacing.COUNT];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = new IntArrayList();
    }
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferCache> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        if (buildBuffers == null)
            reload();
        
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        ((LittleLevelSliceExtender) (Object) slice).setLevel(renderLevel);
        ((BlockRenderContextAccessor) context).setSlice(slice);
        
        BlockPos pos = data.be.getBlockPos();
        
        lightAccess.prepare(renderLevel);
        
        LightPipeline lighter = lighters.getLighter(((BlockRendererAccessor) renderer).getUseAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(),
            pos) == 0 ? LightMode.SMOOTH : LightMode.FLAT);
        
        ColorProviderRegistry colorProvider = ((BlockRendererAccessor) renderer).getColorProviderRegistry();
        data.prepareModelOffset(modelOffset, pos);
        
        // Render vertex buffer
        for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            Material material = DefaultMaterials.forRenderLayer(entry.getKey());
            ChunkModelBuilder builder = buildBuffers.get(material);
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            for (int i = 0; i < ModelQuadFacing.VALUES.length; i++)
                builder.getVertexBuffer(ModelQuadFacing.VALUES[i]).start(data.sectionIndex());
            
            Arrays.fill(faceCounters, 0);
            
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i].add(x);
                    ChunkMeshBufferBuilderAccessor a = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                    indexes[i].add(a.getCount() * a.getStride());
                }
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                context.update(pos, modelOffset, state, null, 0);
                //OculusManager.setLocalPos(buildBuffers, pos);
                cubeCenter.set((cube.maxX + cube.minX) * 0.5, (cube.maxY + cube.minY) * 0.5, (cube.maxZ + cube.minZ) * 0.5);
                
                ColorProvider<BlockState> colorizer = null;
                
                /*if (OculusManager.isShaders()) {
                    if (state.getBlock() instanceof IFakeRenderingBlock fake)
                        state = fake.getFakeState(state);
                    OculusManager.setMaterialId(buildBuffers, state);
                }*/
                
                for (int h = 0; h < Facing.VALUES.length; h++) {
                    Facing facing = Facing.VALUES[h];
                    Object quadObject = cube.getQuad(facing);
                    List<BakedQuad> quads = null;
                    if (quadObject instanceof List) {
                        quads = (List<BakedQuad>) quadObject;
                    } else if (quadObject instanceof BakedQuad quad) {
                        bakedQuadWrapper.setElement(quad);
                        quads = bakedQuadWrapper;
                    }
                    if (quads != null && !quads.isEmpty()) {
                        Direction direction = facing.toVanilla();
                        
                        for (BakedQuad quad : quads) {
                            lighter.calculate((ModelQuadView) quad, pos, cachedQuadLightData, direction, ((BakedQuadView) quad).getLightFace(), quad.isShade(), true);
                            
                            if (cube.color != -1) {
                                int color = ColorABGR.pack(ColorUtils.red(cube.color), ColorUtils.green(cube.color), ColorUtils.blue(cube.color), ColorUtils.alpha(cube.color));
                                Arrays.fill(colors, color);
                            } else if (quad.isTinted()) {
                                if (colorizer == null)
                                    colorizer = colorProvider.getColorProvider(state.getBlock());
                                
                                colorizer.getColors(slice, pos, scratchColorPos, state, (ModelQuadView) quad, colors);
                            } else
                                Arrays.fill(colors, -1);
                            
                            ((BlockRendererAccessor) renderer).callWriteGeometry(context, builder, Vec3.ZERO, material, (BakedQuadView) quad, colors, cachedQuadLightData);
                            TextureAtlasSprite sprite = quad.getSprite();
                            if (sprite != null && SpriteUtil.hasAnimation(sprite))
                                sprites.add(sprite);
                        }
                    }
                }
                
                bakedQuadWrapper.setElement(null);
                
                //OculusManager.resetBlockContext(buildBuffers);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            BufferHolder[] holders = new BufferHolder[ModelQuadFacing.COUNT];
            int count = indexes[0].size() / 2;
            for (int i = 0; i < indexes.length; i++) {
                if (material.pass.isTranslucent() && i != ModelQuadFacing.UNASSIGNED.ordinal())
                    continue;
                ChunkMeshBufferBuilderAccessor v = (ChunkMeshBufferBuilderAccessor) builder.getVertexBuffer(ModelQuadFacing.VALUES[i]);
                if (v.getCount() > 0) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(v.getStride() * v.getCount());
                    ByteBuffer threadBuffer = v.getBuffer();
                    threadBuffer.limit(buffer.capacity());
                    MemoryUtil.memCopy(v.getBuffer(), buffer);
                    threadBuffer.limit(threadBuffer.capacity());
                    holders[i] = new BufferHolder(buffer, buffer.limit(), v.getCount(), indexes[i].toIntArray());
                    indexes[i].clear();
                    
                    /*if (material.pass.isTranslucent()) { TODO Properly implement translucent stuff
                        var centers = ((TranslucentQuadAnalyzerAccessor) context.collector).getQuadCenters();
                        ByteBuffer centerBuffer = ByteBuffer.allocateDirect(centers.size() * 4);
                        for (int j = 0; j < centers.size(); j++)
                            centerBuffer.putFloat(centers.getFloat(j));
                        centerBuffer.rewind();
                        holders[0] = new BufferHolder(centerBuffer, 0, 0, null);
                        centers.clear(); // Reset for next renderer
                    }*/
                }
            }
            
            buffers.put(entry.key, new EmbeddiumBufferCache(holders, new ArrayList<>(sprites), count));
            sprites.clear();
        }
        
        ((LittleLevelSliceExtender) (Object) slice).setLevel(null);
    }
    
    @Override
    public void reload() {
        RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
        if (manager == null) {
            buildBuffers = null;
            renderer = null;
            return;
        }
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
        type = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType();
        buildBuffers = new ChunkBuildBuffers(type);
        buildBuffers.init(null, 0);
        renderer = new BlockRenderer(new ColorProviderRegistry(Minecraft.getInstance()
                .getBlockColors()), lighters = new LightPipelineProvider(lightAccess = new LittleLightDataAccess()));
    }
    
    @Override
    public void release() {
        buildBuffers.destroy();
    }
    
}
