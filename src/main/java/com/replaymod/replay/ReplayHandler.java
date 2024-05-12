package com.replaymod.replay;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.replaymod.core.ReplayMod;
import com.replaymod.mixin.EntityLivingBaseAccessor;
import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.TimerAccessor;
import com.replaymod.core.utils.Restrictions;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiProgressBar;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.camera.SpectatorCameraController;
import com.replaymod.replay.events.ReplayClosedCallback;
import com.replaymod.replay.events.ReplayClosingCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.util.Location;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class ReplayHandler {
   private static final Minecraft mc = MCVer.getMinecraft();
   private final ReplayFile replayFile;
   private final FullReplaySender fullReplaySender;
   private final QuickReplaySender quickReplaySender;
   private boolean quickMode = false;
   private Restrictions restrictions = new Restrictions();
   private boolean suppressCameraMovements;
   private final Set<Marker> markers;
   private final GuiReplayOverlay overlay;
   private EmbeddedChannel channel;
   private final int replayDuration;
   private Location targetCameraPosition;
   private UUID spectating;

   public ReplayHandler(ReplayFile replayFile, boolean asyncMode) throws IOException {
      Preconditions.checkState(mc.isSameThread(), "Must be called from Minecraft thread.");
      this.replayFile = replayFile;
      this.replayDuration = replayFile.getMetaData().getDuration();
      this.markers = replayFile.getMarkers().or(Collections.emptySet());
      this.fullReplaySender = new FullReplaySender(this, replayFile, false);
      this.quickReplaySender = new QuickReplaySender(ReplayModReplay.instance, replayFile);
      this.setup();
      this.overlay = new GuiReplayOverlay(this);
      this.overlay.setVisible(true);
      ReplayOpenedCallback.EVENT.invoker().replayOpened(this);
      this.fullReplaySender.setAsyncMode(asyncMode);
   }

   void restartedReplay() {
      Preconditions.checkState(mc.isSameThread(), "Must be called from Minecraft thread.");
      this.channel.close();
      mc.mouseHandler.releaseMouse();
      mc.clearLevel();
      this.restrictions = new Restrictions();
      this.setup();
   }

   public void endReplay() throws IOException {
      Preconditions.checkState(mc.isSameThread(), "Must be called from Minecraft thread.");
      ReplayClosingCallback.EVENT.invoker().replayClosing(this);
      this.fullReplaySender.terminateReplay();
      if (this.quickMode) {
         this.quickReplaySender.unregister();
      }

      this.replayFile.save();
      this.replayFile.close();
      this.channel.close().awaitUninterruptibly();
      if (mc.player instanceof CameraEntity) {
      }

      if (mc.level != null) {
         mc.clearLevel();
      }

      TimerAccessor timer = (TimerAccessor)((MinecraftAccessor)mc).getTimer();
      timer.setTickLength(50.0F);
      this.overlay.setVisible(false);
      ReplayModReplay.instance.forcefullyStopReplay();
      mc.setScreen(null);
      ReplayClosedCallback.EVENT.invoker().replayClosed(this);
   }

   private void setup() {
      Preconditions.checkState(mc.isSameThread(), "Must be called from Minecraft thread.");
      mc.gui.getChat().clearMessages(false);
      Connection networkManager = new Connection(PacketFlow.CLIENTBOUND) {
         public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
            t.printStackTrace();
         }
      };
      networkManager.setListener(new ClientHandshakePacketListenerImpl(networkManager, mc, null, null, false, null, (it) -> {
      }));
      this.channel = new EmbeddedChannel();
      this.channel.pipeline().addLast("ReplayModReplay_quickReplaySender", this.quickReplaySender);
      this.channel.pipeline().addLast("ReplayModReplay_replaySender", this.fullReplaySender);
      this.channel.pipeline().addLast("bundler", new PacketBundlePacker(PacketFlow.CLIENTBOUND));
      this.channel.pipeline().addLast("packet_handler", networkManager);
      this.channel.pipeline().fireChannelActive();
      networkManager.setProtocol(ConnectionProtocol.LOGIN);
      ((MinecraftAccessor)mc).setConnection(networkManager);
      NetworkHooks.registerClientLoginChannel(networkManager);
   }

   public ReplayFile getReplayFile() {
      return this.replayFile;
   }

   public Restrictions getRestrictions() {
      return this.restrictions;
   }

   public ReplaySender getReplaySender() {
      return this.quickMode ? this.quickReplaySender : this.fullReplaySender;
   }

   public GuiReplayOverlay getOverlay() {
      return this.overlay;
   }

   public void ensureQuickModeInitialized(Runnable andThen) {
      if (!Utils.ifMinimalModeDoPopup(this.overlay, () -> {
      })) {
         ListenableFuture<Void> future = this.quickReplaySender.getInitializationPromise();
         if (future == null) {
            final ReplayHandler.InitializingQuickModePopup popup = new ReplayHandler.InitializingQuickModePopup(this.overlay);
            future = this.quickReplaySender.initialize((progress) -> {
               popup.progressBar.setProgress(progress.floatValue());
            });
            Futures.addCallback(future, new FutureCallback<Void>() {
               public void onSuccess(@Nullable Void result) {
                  popup.close();
               }

               public void onFailure(@Nonnull Throwable t) {
                  String message = "Failed to initialize quick mode. It will not be available.";
                  Logger var10000 = ReplayModReplay.LOGGER;
                  CrashReport var10002 = CrashReport.forThrowable(t, message);
                  Objects.requireNonNull(popup);
                  Utils.error(var10000, ReplayHandler.this.overlay, var10002, popup::close);
               }
            }, Runnable::run);
         }

         Futures.addCallback(future, new FutureCallback<>() {
            public void onSuccess(@Nullable Void result) {
               andThen.run();
            }

            public void onFailure(@Nonnull Throwable t) {
            }
         }, Runnable::run);
      }
   }

   public void setQuickMode(boolean quickMode) {
      if (ReplayMod.isMinimalMode()) {
         throw new UnsupportedOperationException("Quick Mode not supported in minimal mode.");
      } else if (quickMode != this.quickMode) {
         if (quickMode && this.fullReplaySender.isAsyncMode()) {
            throw new IllegalStateException("Cannot switch to quick mode while in async mode.");
         } else {
            this.quickMode = quickMode;
            CameraEntity cam = this.getCameraEntity();
            if (cam != null) {
               this.targetCameraPosition = new Location(cam.getX(), cam.getY(), cam.getZ(), cam.getYRot(), cam.getXRot());
            } else {
               this.targetCameraPosition = null;
            }

            if (quickMode) {
               this.quickReplaySender.register();
               this.quickReplaySender.restart();
               this.quickReplaySender.sendPacketsTill(this.fullReplaySender.currentTimeStamp());
            } else {
               this.quickReplaySender.unregister();
               this.fullReplaySender.sendPacketsTill(0);
               this.fullReplaySender.sendPacketsTill(this.quickReplaySender.currentTimeStamp());
            }

            this.moveCameraToTargetPosition();
         }
      }
   }

   public boolean isQuickMode() {
      return this.quickMode;
   }

   public int getReplayDuration() {
      return this.replayDuration;
   }

   public boolean shouldSuppressCameraMovements() {
      return this.suppressCameraMovements;
   }

   public void setSuppressCameraMovements(boolean suppressCameraMovements) {
      this.suppressCameraMovements = suppressCameraMovements;
   }

   public void spectateEntity(Entity e) {
      CameraEntity cameraEntity = this.getCameraEntity();
      if (cameraEntity != null) {
         if (e != null && e != cameraEntity) {
            if (e instanceof Player) {
               this.spectating = e.getUUID();
            }
         } else {
            this.spectating = null;
            e = cameraEntity;
         }

         if (e == cameraEntity) {
            cameraEntity.setCameraController(ReplayModReplay.instance.createCameraController(cameraEntity));
         } else {
            cameraEntity.setCameraController(new SpectatorCameraController(cameraEntity));
         }

         if (mc.getCameraEntity() != e) {
            mc.setCameraEntity(e);
            cameraEntity.setCameraPosRot(e);
         }

      }
   }

   public void spectateCamera() {
      this.spectateEntity(null);
   }

   public boolean isCameraView() {
      return mc.player instanceof CameraEntity && mc.player == mc.getCameraEntity();
   }

   public CameraEntity getCameraEntity() {
      return mc.player instanceof CameraEntity ? (CameraEntity)mc.player : null;
   }

   public UUID getSpectatedUUID() {
      return this.spectating;
   }

   public void moveCameraToTargetPosition() {
      CameraEntity cam = this.getCameraEntity();
      if (cam != null && this.targetCameraPosition != null) {
         cam.setCameraPosRot(this.targetCameraPosition);
      }

   }

   public void doJump(int targetTime, boolean retainCameraPosition) {
      if (this.getReplaySender().isAsyncMode()) {
         if (this.getReplaySender() == this.quickReplaySender) {
            targetTime %= 50;
            if (targetTime >= 50) {
               this.quickReplaySender.sendPacketsTill(targetTime - 50);
            }

            Iterator var11;
            Entity entity;
            for(var11 = mc.level.entitiesForRendering().iterator(); var11.hasNext(); entity.xRotO = entity.getXRot()) {
               entity = (Entity)var11.next();
               this.skipTeleportInterpolation(entity);
               entity.xOld = entity.xo = entity.getX();
               entity.yOld = entity.yo = entity.getY();
               entity.zOld = entity.zo = entity.getZ();
               entity.yRotO = entity.getYRot();
            }

            mc.tick();
            this.quickReplaySender.sendPacketsTill(targetTime);
            var11 = mc.level.entitiesForRendering().iterator();

            while(var11.hasNext()) {
               entity = (Entity)var11.next();
               this.skipTeleportInterpolation(entity);
            }

         } else {
            FullReplaySender replaySender = this.fullReplaySender;
            if (!replaySender.isHurrying()) {
               if (targetTime < replaySender.currentTimeStamp()) {
                  mc.setScreen(null);
               }

               if (retainCameraPosition) {
                  CameraEntity cam = this.getCameraEntity();
                  if (cam != null) {
                     this.targetCameraPosition = new Location(cam.getX(), cam.getY(), cam.getZ(), cam.getYRot(), cam.getXRot());
                  } else {
                     this.targetCameraPosition = null;
                  }
               }

               long diff = (long)targetTime - (replaySender.isHurrying() ? replaySender.getDesiredTimestamp() : (long)replaySender.currentTimeStamp());
               if (diff != 0L) {
                  if (diff > 0L && diff < 5000L) {
                     replaySender.jumpToTime(targetTime);
                  } else {
                     GuiScreen guiScreen = new GuiScreen();
                     guiScreen.setBackground(AbstractGuiScreen.Background.DIRT);
                     guiScreen.setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER));
                     guiScreen.addElements(new HorizontalLayout.Data(0.5D), (new GuiLabel()).setI18nText("replaymod.gui.pleasewait"));
                     replaySender.setSyncModeAndWait();
                     MCVer.pushMatrix();
                     RenderSystem.clear(16640, true);
                     mc.getMainRenderTarget().bindWrite(true);
                     Window window = mc.getWindow();
                     RenderSystem.clear(256, Minecraft.ON_OSX);
                     RenderSystem.setProjectionMatrix(MCVer.ortho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), 0.0F, (float)((double)window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
                     PoseStack matrixStack = RenderSystem.getModelViewStack();
                     matrixStack.setIdentity();
                     matrixStack.translate(0.0F, 0.0F, -2000.0F);
                     RenderSystem.applyModelViewMatrix();
                     Lighting.setupFor3DItems();
                     guiScreen.toMinecraft().init(mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());
                     guiScreen.toMinecraft().render(new GuiGraphics(mc, mc.renderBuffers().bufferSource()), 0, 0, 0.0F);
                     guiScreen.toMinecraft().removed();
                     mc.getMainRenderTarget().unbindWrite();
                     MCVer.popMatrix();
                     MCVer.pushMatrix();
                     mc.getMainRenderTarget().blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                     MCVer.popMatrix();
                     mc.getWindow().updateDisplay();

                     do {
                        do {
                           replaySender.sendPacketsTill(targetTime);
                           targetTime += 500;
                        } while(mc.player == null);
                     } while(mc.screen instanceof ReceivingLevelScreen);

                     replaySender.setAsyncMode(true);
                     replaySender.setReplaySpeed(0.0D);
                     mc.getConnection().getConnection().tick();
                     if (mc.level == null) {
                        return;
                     }

                     Entity entity;
                     for(Iterator var9 = mc.level.entitiesForRendering().iterator(); var9.hasNext(); entity.xRotO = entity.getXRot()) {
                        entity = (Entity)var9.next();
                        this.skipTeleportInterpolation(entity);
                        entity.xOld = entity.xo = entity.getX();
                        entity.yOld = entity.yo = entity.getY();
                        entity.zOld = entity.zo = entity.getZ();
                        entity.yRotO = entity.getYRot();
                     }

                     mc.tick();
                     this.moveCameraToTargetPosition();
                  }
               }

            }
         }
      }
   }

   private void skipTeleportInterpolation(Entity entity) {
      if (entity instanceof LivingEntity e && !(entity instanceof CameraEntity)) {
         EntityLivingBaseAccessor ea = (EntityLivingBaseAccessor)e;
         e.absMoveTo(ea.getInterpTargetX(), ea.getInterpTargetY(), ea.getInterpTargetZ());
         e.setYRot((float)ea.getInterpTargetYaw());
         e.setXRot((float)ea.getInterpTargetPitch());
      }

   }

   private class InitializingQuickModePopup extends AbstractGuiPopup<ReplayHandler.InitializingQuickModePopup> {
      private final GuiProgressBar progressBar;

      public InitializingQuickModePopup(GuiContainer container) {
         super(container);
         this.progressBar = (new GuiProgressBar(this.popup)).setSize(300, 20).setI18nLabel("replaymod.gui.loadquickmode", new Object[0]);
         this.open();
      }

      public void close() {
         super.close();
      }

      protected ReplayHandler.InitializingQuickModePopup getThis() {
         return this;
      }
   }
}
