package com.replaymod.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.recording.packet.ResourcePackRecorder;
import java.io.File;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({DownloadedPackSource.class})
public abstract class MixinDownloadingPackFinder implements ResourcePackRecorder.IDownloadingPackFinder {
   private Consumer<File> requestCallback;

   public void setRequestCallback(Consumer<File> callback) {
      this.requestCallback = callback;
   }

   @Inject(
      method = {"setServerPack"},
      at = {@At("HEAD")}
   )
   private void recordDownloadedPack(File file, PackSource arg, CallbackInfoReturnable ci) {
      if (this.requestCallback != null) {
         this.requestCallback.consume(file);
         this.requestCallback = null;
      }

   }
}
