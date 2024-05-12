package com.replaymod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import com.replaymod.render.hooks.IForceChunkLoading;
import com.replaymod.render.utils.FlawlessFrames;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public abstract class Mixin_ForceChunkLoading implements IForceChunkLoading {
   private ForceChunkLoadingHook replayModRender_hook;
   @Shadow
   private ChunkRenderDispatcher chunkRenderDispatcher;
   @Shadow
   private Frustum cullingFrustum;
   @Shadow
   private Frustum capturedFrustum;
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   @Final
   private ObjectArrayList<ChunkInfoAccessor> renderChunksInFrustum;
   @Shadow
   private boolean needsFullRenderChunkUpdate;
   @Shadow
   @Final
   private BlockingQueue<RenderChunk> recentlyCompiledChunks;
   @Shadow
   private Future<?> lastFullRenderChunkUpdate;
   @Shadow
   @Final
   private AtomicBoolean needsFrustumUpdate;

   public void replayModRender_setHook(ForceChunkLoadingHook hook) {
      this.replayModRender_hook = hook;
   }

   @Shadow
   protected abstract void setupRender(Camera var1, Frustum var2, boolean var3, boolean var4);

   @Shadow
   protected abstract void applyFrustum(Frustum var1);

   @Inject(
      method = {"renderLevel"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"
)}
   )
   private void forceAllChunks(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
      if (this.replayModRender_hook != null) {
         if (!FlawlessFrames.hasSodium()) {

            RenderRegionCache chunkRendererRegionBuilder = new RenderRegionCache();

            do {
               this.setupRender(camera, this.cullingFrustum, this.capturedFrustum != null, this.minecraft.player.isSpectator());
               if (this.lastFullRenderChunkUpdate != null) {
                  try {
                     this.lastFullRenderChunkUpdate.get(5L, TimeUnit.SECONDS);
                  } catch (InterruptedException var15) {
                     Thread.currentThread().interrupt();
                     return;
                  } catch (ExecutionException var16) {
                     throw new RuntimeException(var16);
                  } catch (TimeoutException var17) {
                     var17.printStackTrace();
                  }
               }

               if (this.needsFrustumUpdate.compareAndSet(true, false)) {
                  this.applyFrustum((new Frustum(this.cullingFrustum)).offsetToFullyIncludeCameraCube(8));
               }

               for (ChunkInfoAccessor chunkInfo : this.renderChunksInFrustum) {
                  RenderChunk builtChunk = chunkInfo.getChunk();
                  if (builtChunk.isDirty()) {
                     if (builtChunk.hasAllNeighbors()) {
                        builtChunk.rebuildChunkAsync(this.chunkRenderDispatcher, chunkRendererRegionBuilder);
                     }

                     builtChunk.setNotDirty();
                  }
               }

               this.needsFullRenderChunkUpdate |= ((ForceChunkLoadingHook.IBlockOnChunkRebuilds)this.chunkRenderDispatcher).uploadEverythingBlocking();
            } while(this.needsFullRenderChunkUpdate || !this.recentlyCompiledChunks.isEmpty());

         }
      }
   }
}
