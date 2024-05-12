package com.replaymod.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public abstract class MixinClientConnection {
   @Shadow
   private Channel channel;

   @Inject(
      method = {"setupCompression"},
      at = {@At("RETURN")}
   )
   private void ensureReplayModRecorderIsAfterDecompress(CallbackInfo ci) {
      ChannelHandler recorder = null;
      Iterator<Entry<String, ChannelHandler>> var3 = this.channel.pipeline().iterator();

      String key;
      if (!var3.hasNext()) {
         return;
      }

      Entry<String, ChannelHandler> entry = var3.next();
      key = entry.getKey();
      if ("replay_recorder_raw".equals(key)) {
         recorder = entry.getValue();
      }
      while (!"decompress".equals(key) || recorder == null) {
         if (!var3.hasNext()) {
            return;
         }

         entry = var3.next();
         key = entry.getKey();
         if ("replay_recorder_raw".equals(key)) {
            recorder = entry.getValue();
         }
      }

      this.channel.pipeline().remove(recorder);
      this.channel.pipeline().addBefore("decoder", "replay_recorder_raw", recorder);
   }
}
