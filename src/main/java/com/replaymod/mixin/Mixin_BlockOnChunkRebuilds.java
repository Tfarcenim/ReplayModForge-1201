package com.replaymod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.render.hooks.ForceChunkLoadingHook;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.thread.ProcessorMailbox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ChunkRenderDispatcher.class})
public abstract class Mixin_BlockOnChunkRebuilds implements ForceChunkLoadingHook.IBlockOnChunkRebuilds {
   @Shadow
   @Final
   private Queue<ChunkBufferBuilderPack> freeBuffers;
   @Shadow
   @Final
   private ProcessorMailbox<Runnable> mailbox;
   @Shadow
   @Final
   private Queue<Runnable> toUpload;
   private final Lock waitingForWorkLock = new ReentrantLock();
   private final Condition newWork = this.waitingForWorkLock.newCondition();
   private volatile boolean allDone;
   private int totalBufferCount;

   @Unique
   private boolean upload() {
      boolean anything;
      Runnable runnable;
      for(anything = false; (runnable = this.toUpload.poll()) != null; anything = true) {
         runnable.run();
      }

      return anything;
   }

   @Shadow
   protected abstract void runTask();

   @Inject(
      method = {"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/renderer/LevelRenderer;Ljava/util/concurrent/Executor;ZLnet/minecraft/client/renderer/ChunkBufferBuilderPack;I)V"},
      at = {@At("RETURN")}
   )
   private void rememberTotalThreads(CallbackInfo ci) {
      this.totalBufferCount = this.freeBuffers.size();
   }

   @Inject(
      method = {"runTask"},
      at = {@At("RETURN")}
   )
   private void notifyMainThreadIfEverythingIsDone(CallbackInfo ci) {
      if (this.freeBuffers.size() == this.totalBufferCount) {
         this.waitingForWorkLock.lock();

         try {
            this.allDone = true;
            this.newWork.signalAll();
         } finally {
            this.waitingForWorkLock.unlock();
         }
      } else {
         this.allDone = false;
      }

   }

   @Inject(
      method = {"uploadChunkLayer"},
      at = {@At("RETURN")}
   )
   private void notifyMainThreadOfNewUpload(CallbackInfoReturnable<CompletableFuture<Void>> ci) {
      this.waitingForWorkLock.lock();

      try {
         this.newWork.signal();
      } finally {
         this.waitingForWorkLock.unlock();
      }

   }

   private boolean waitForMainThreadWork() {
      boolean allDone = (Boolean)this.mailbox.ask((reply) -> {
         return () -> {
            this.runTask();
            reply.tell(this.freeBuffers.size() == this.totalBufferCount);
         };
      }).join();
      if (allDone) {
         return true;
      } else {
         this.waitingForWorkLock.lock();

         try {
            while(true) {
               RenderSystem.replayQueue();
               boolean var2;
               if (this.allDone) {
                  var2 = true;
                  return var2;
               }

               if (!this.toUpload.isEmpty()) {
                  var2 = false;
                  return var2;
               }

               this.newWork.awaitUninterruptibly();
            }
         } finally {
            this.waitingForWorkLock.unlock();
         }
      }
   }

   public boolean uploadEverythingBlocking() {
      boolean anything = false;

      boolean allChunksBuilt;
      do {
         for(allChunksBuilt = this.waitForMainThreadWork(); this.upload(); anything = true) {
         }
      } while(!allChunksBuilt);

      return anything;
   }
}
