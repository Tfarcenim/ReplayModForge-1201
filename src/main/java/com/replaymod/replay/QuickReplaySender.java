package com.replaymod.replay;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.TimerAccessor;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.rar.RandomAccessReplay;
import com.replaymod.replaystudio.replay.ReplayFile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import java.io.IOException;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;

@Sharable
public class QuickReplaySender extends ChannelHandlerAdapter implements ReplaySender {
   private final Minecraft mc = MCVer.getMinecraft();
   private final ReplayModReplay mod;
   private final RandomAccessReplay replay;
   private final QuickReplaySender.EventHandler eventHandler = new QuickReplaySender.EventHandler();
   private ChannelHandlerContext ctx;
   private int currentTimeStamp;
   private double replaySpeed = 1.0D;
   private boolean asyncMode;
   private long lastAsyncUpdateTime;
   private ListenableFuture<Void> initPromise;

   public QuickReplaySender(ReplayModReplay mod, ReplayFile replayFile) {
      this.mod = mod;
      this.replay = new RandomAccessReplay(replayFile, MCVer.getPacketTypeRegistry(false)) {
         private byte[] buf = new byte[0];

         protected void dispatch(Packet packet) {
            com.github.steveice10.netty.buffer.ByteBuf byteBuf = packet.getBuf();
            int size = byteBuf.readableBytes();
            if (this.buf.length < size) {
               this.buf = new byte[size];
            }

            byteBuf.getBytes(byteBuf.readerIndex(), this.buf, 0, size);
            ByteBuf wrappedBuf = Unpooled.wrappedBuffer(this.buf);
            wrappedBuf.writerIndex(size);
            FriendlyByteBuf packetByteBuf = new FriendlyByteBuf(wrappedBuf);
            net.minecraft.network.protocol.Packet<?> mcPacket = ConnectionProtocol.PLAY.createPacket(PacketFlow.CLIENTBOUND, packet.getId(), packetByteBuf);
            if (mcPacket != null) {
               QuickReplaySender.this.ctx.fireChannelRead(mcPacket);
            }

         }
      };
   }

   public void register() {
      this.eventHandler.register();
   }

   public void unregister() {
      this.eventHandler.unregister();
   }

   public void handlerAdded(ChannelHandlerContext ctx) {
      this.ctx = ctx;
   }

   public ListenableFuture<Void> getInitializationPromise() {
      return this.initPromise;
   }

   public ListenableFuture<Void> initialize(Consumer<Double> progress) {
      if (this.initPromise != null) {
         return this.initPromise;
      } else {
         SettableFuture<Void> promise = SettableFuture.create();
         this.initPromise = promise;
         (new Thread(() -> {
            try {
               long start = System.currentTimeMillis();
               this.replay.load(progress);
               ReplayModReplay.LOGGER.info("Initialized quick replay sender in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Throwable var5) {
               ReplayModReplay.LOGGER.error("Initializing quick replay sender:", var5);
               this.mod.getCore().runLaterWithoutLock(() -> {
                  this.mod.getCore().printWarningToChat("Error initializing quick replay sender: %s", var5.getLocalizedMessage());
                  promise.setException(var5);
               });
               return;
            }

            this.mod.getCore().runLaterWithoutLock(() -> {
               promise.set(null);
            });
         })).start();
         return promise;
      }
   }

   private void ensureInitialized(Runnable body) {
      if (this.initPromise == null) {
         ReplayModReplay.LOGGER.warn("QuickReplaySender used without prior initialization!", new Throwable());
         this.initialize((progress) -> {
         });
      }

      Futures.addCallback(this.initPromise, new FutureCallback<Void>() {
         public void onSuccess(@Nullable Void result) {
            body.run();
         }

         public void onFailure(Throwable t) {
         }
      }, Runnable::run);
   }

   public void restart() {
      this.replay.reset();
   }

   public int currentTimeStamp() {
      return this.currentTimeStamp;
   }

   public void setReplaySpeed(double factor) {
      if (factor != 0.0D) {
         if (this.paused() && this.asyncMode) {
            this.lastAsyncUpdateTime = System.currentTimeMillis();
         }

         this.replaySpeed = factor;
      }

      TimerAccessor timer = (TimerAccessor)((MinecraftAccessor)this.mc).getTimer();
      timer.setTickLength(50.0F / (float)factor);
   }

   public double getReplaySpeed() {
      return this.replaySpeed;
   }

   public boolean isAsyncMode() {
      return this.asyncMode;
   }

   public void setAsyncMode(boolean async) {
      if (this.asyncMode != async) {
         this.ensureInitialized(() -> {
            this.asyncMode = async;
            if (async) {
               this.lastAsyncUpdateTime = System.currentTimeMillis();
            }

         });
      }
   }

   public void setSyncModeAndWait() {
      this.setAsyncMode(false);
   }

   public void jumpToTime(int value) {
      this.sendPacketsTill(value);
   }

   public void sendPacketsTill(int replayTime) {
      this.ensureInitialized(() -> {
         try {
            this.replay.seek(replayTime);
         } catch (IOException var3) {
            var3.printStackTrace();
         }

         this.currentTimeStamp = replayTime;
      });
   }

   private class EventHandler extends EventRegistrations {
      private EventHandler() {
         this.on(PreTickCallback.EVENT, this::onTick);
      }

      private void onTick() {
         if (QuickReplaySender.this.asyncMode && !QuickReplaySender.this.paused()) {
            long now = System.currentTimeMillis();
            long realTimePassed = now - QuickReplaySender.this.lastAsyncUpdateTime;
            QuickReplaySender.this.lastAsyncUpdateTime = now;
            int replayTimePassed = (int)((double)realTimePassed * QuickReplaySender.this.replaySpeed);
            QuickReplaySender.this.sendPacketsTill(QuickReplaySender.this.currentTimeStamp + replayTimePassed);
         }
      }
   }
}
