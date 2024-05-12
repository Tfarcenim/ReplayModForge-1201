package com.replaymod.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   targets = {"net.minecraft.client.renderer.LevelRenderer$RenderChunkInfo"}
)
public interface ChunkInfoAccessor {
   @Accessor
   RenderChunk getChunk();
}
