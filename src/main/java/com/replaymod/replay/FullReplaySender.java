package com.replaymod.replay;

import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.replaymod.core.ReplayMod;
import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.TimerAccessor;
import com.replaymod.core.utils.Restrictions;
import com.replaymod.core.versions.MCVer;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

@Sharable
public class FullReplaySender extends ChannelDuplexHandler implements ReplaySender {
   private static final List<Class> BAD_PACKETS = Arrays.asList(ClientboundBlockChangedAckPacket.class, ClientboundOpenBookPacket.class, ClientboundOpenScreenPacket.class, ClientboundUpdateRecipesPacket.class, ClientboundUpdateAdvancementsPacket.class, ClientboundSelectAdvancementsTabPacket.class, ClientboundSetCameraPacket.class, ClientboundSetTitleTextPacket.class, ClientboundSetHealthPacket.class, ClientboundHorseScreenOpenPacket.class, ClientboundContainerClosePacket.class, ClientboundContainerSetSlotPacket.class, ClientboundContainerSetDataPacket.class, ClientboundOpenSignEditorPacket.class, ClientboundAwardStatsPacket.class, ClientboundSetExperiencePacket.class, ClientboundPlayerAbilitiesPacket.class);
   private static final int TP_DISTANCE_LIMIT = 128;
   private final ReplayHandler replayHandler;
   protected boolean asyncMode;
   protected int lastTimeStamp;
   protected int currentTimeStamp;
   protected ReplayFile replayFile;
   protected ChannelHandlerContext ctx;
   protected ReplayInputStream replayIn;
   protected FullReplaySender.PacketData nextPacket;
   private boolean loginPhase = true;
   protected boolean startFromBeginning = true;
   protected boolean terminate;
   protected double replaySpeed = 1.0D;
   protected boolean hasWorldLoaded;
   protected Minecraft mc = MCVer.getMinecraft();
   protected final int replayLength;
   protected int actualID = -1;
   protected boolean allowMovement;
   private final File tempResourcePackFolder = Files.createTempDir();
   private final FullReplaySender.EventHandler events = new FullReplaySender.EventHandler();
   private long realTimeStart;
   private double realTimeStartSpeed;
   private long desiredTimeStamp = -1L;
   private final Runnable asyncSender = new Runnable() {
      public void run() {
         try {
            while(FullReplaySender.this.ctx == null && !FullReplaySender.this.terminate) {
               Thread.sleep(10L);
            }

            while(!FullReplaySender.this.terminate) {
               synchronized(FullReplaySender.this) {
                  if (FullReplaySender.this.replayIn == null) {
                     FullReplaySender.this.replayIn = FullReplaySender.this.replayFile.getPacketData(MCVer.getPacketTypeRegistry(true));
                  }

                  while(true) {
                     try {
                        while(FullReplaySender.this.paused() && FullReplaySender.this.hasWorldLoaded && !FullReplaySender.this.terminate && !FullReplaySender.this.startFromBeginning && FullReplaySender.this.desiredTimeStamp == -1L) {
                           Thread.sleep(10L);
                        }

                        if (FullReplaySender.this.terminate) {
                           return;
                        }

                        if (FullReplaySender.this.startFromBeginning) {
                           break;
                        }

                        if (FullReplaySender.this.nextPacket == null) {
                           FullReplaySender.this.nextPacket = new FullReplaySender.PacketData(FullReplaySender.this.replayIn, FullReplaySender.this.loginPhase);
                        }

                        int nextTimeStamp = FullReplaySender.this.nextPacket.timestamp;
                        if (!FullReplaySender.this.isHurrying() && FullReplaySender.this.hasWorldLoaded) {
                           long expectedTime = FullReplaySender.this.realTimeStart + (long)((double)nextTimeStamp / FullReplaySender.this.replaySpeed);
                           long now = System.currentTimeMillis();
                           if (expectedTime > now) {
                              Thread.sleep(expectedTime - now);
                           }
                        }

                        FullReplaySender.this.channelRead(FullReplaySender.this.ctx, FullReplaySender.this.nextPacket.bytes);
                        FullReplaySender.this.nextPacket = null;
                        FullReplaySender.this.lastTimeStamp = nextTimeStamp;
                        if (FullReplaySender.this.isHurrying() && (long)FullReplaySender.this.lastTimeStamp > FullReplaySender.this.desiredTimeStamp && !FullReplaySender.this.startFromBeginning) {
                           FullReplaySender.this.desiredTimeStamp = -1L;
                           FullReplaySender.this.replayHandler.moveCameraToTargetPosition();
                           FullReplaySender.this.setReplaySpeed(0.0D);
                        }
                     } catch (EOFException var8) {
                        FullReplaySender.this.setReplaySpeed(0.0D);

                        while(FullReplaySender.this.paused() && FullReplaySender.this.hasWorldLoaded && FullReplaySender.this.desiredTimeStamp == -1L && !FullReplaySender.this.terminate) {
                           Thread.sleep(10L);
                        }

                        if (FullReplaySender.this.terminate) {
                           return;
                        }
                        break;
                     } catch (IOException var9) {
                        var9.printStackTrace();
                     }
                  }

                  FullReplaySender.this.hasWorldLoaded = false;
                  FullReplaySender.this.lastTimeStamp = 0;
                  FullReplaySender.this.loginPhase = true;
                  FullReplaySender.this.startFromBeginning = false;
                  FullReplaySender.this.nextPacket = null;
                  FullReplaySender.this.realTimeStart = System.currentTimeMillis();
                  if (FullReplaySender.this.replayIn != null) {
                     FullReplaySender.this.replayIn.close();
                     FullReplaySender.this.replayIn = null;
                  }

                  ReplayMod var10000 = ReplayMod.instance;
                  ReplayHandler var10001 = FullReplaySender.this.replayHandler;
                  Objects.requireNonNull(var10001);
                  var10000.runSync(var10001::restartedReplay);
               }
            }

         } catch (Exception var11) {
            var11.printStackTrace();
         }
      }
   };
   private final ExecutorService syncSender = Executors.newSingleThreadExecutor((runnable) -> {
      return new Thread(runnable, "replaymod-sync-sender");
   });

   public FullReplaySender(ReplayHandler replayHandler, ReplayFile file, boolean asyncMode) throws IOException {
      this.replayHandler = replayHandler;
      this.replayFile = file;
      this.asyncMode = asyncMode;
      this.replayLength = file.getMetaData().getDuration();
      this.events.register();
      if (asyncMode) {
         (new Thread(this.asyncSender, "replaymod-async-sender")).start();
      }

   }

   public void setAsyncMode(boolean asyncMode) {
      if (this.asyncMode != asyncMode) {
         this.asyncMode = asyncMode;
         if (asyncMode) {
            this.terminate = false;
            (new Thread(this.asyncSender, "replaymod-async-sender")).start();
         } else {
            this.terminate = true;
         }

      }
   }

   public boolean isAsyncMode() {
      return this.asyncMode;
   }

   public void setSyncModeAndWait() {
      if (this.asyncMode) {
         this.asyncMode = false;
         this.terminate = true;
         synchronized(this) {
         }
      }
   }

   public int currentTimeStamp() {
      return this.asyncMode && !this.paused() ? (int)((double)(System.currentTimeMillis() - this.realTimeStart) * this.realTimeStartSpeed) : this.lastTimeStamp;
   }

   public void terminateReplay() {
      if (!this.terminate) {
         this.terminate = true;
         this.syncSender.shutdown();
         this.events.unregister();

         try {
            this.channelInactive(this.ctx);
            this.ctx.channel().pipeline().close();
            FileUtils.deleteDirectory(this.tempResourcePackFolder);
         } catch (Exception var2) {
            var2.printStackTrace();
         }

      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (!this.terminate || !this.asyncMode) {
         if (msg instanceof Packet) {
            super.channelRead(ctx, msg);
         }

         if (msg instanceof byte[]) {
            try {
               Packet p = this.deserializePacket((byte[])msg);
               if (p != null) {
                  p = this.processPacket(p);
                  if (p != null) {
                     super.channelRead(ctx, p);
                  }

                  this.maybeRemoveDeadEntities(p);
                  if (p instanceof ClientboundLevelChunkWithLightPacket) {
                     Runnable doLightUpdates = () -> {
                        ClientLevel world = this.mc.level;
                        if (world != null) {
                           while(!world.isLightUpdateQueueEmpty()) {
                              world.pollLightUpdates();
                           }

                           LevelLightEngine provider = world.getChunkSource().getLightEngine();

                           while(provider.hasLightWork()) {
                              provider.runLightUpdates();
                           }

                        }
                     };
                     if (this.mc.isSameThread()) {
                        doLightUpdates.run();
                     } else {
                        this.mc.tell(doLightUpdates);
                     }
                  }
               }
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

      }
   }

   private Packet deserializePacket(byte[] bytes) throws IOException, IllegalAccessException, InstantiationException {
      ByteBuf bb = Unpooled.wrappedBuffer(bytes);
      FriendlyByteBuf pb = new FriendlyByteBuf(bb);
      int i = pb.readVarInt();
      ConnectionProtocol state = this.loginPhase ? ConnectionProtocol.LOGIN : ConnectionProtocol.PLAY;
      Packet p = state.createPacket(PacketFlow.CLIENTBOUND, i, pb);
      return p;
   }

   private void maybeRemoveDeadEntities(Packet packet) {
      if (!this.asyncMode) {
         boolean relevantPacket = packet instanceof ClientboundAddPlayerPacket || packet instanceof ClientboundAddEntityPacket || packet instanceof ClientboundAddExperienceOrbPacket || packet instanceof ClientboundRemoveEntitiesPacket;
         if (relevantPacket) {
            this.mc.tell(() -> {
               ClientLevel world = this.mc.level;
               if (world != null) {
                  this.removeDeadEntities(world);
               }

            });
         }
      }
   }

   private void removeDeadEntities(ClientLevel world) {
   }

   protected Packet processPacket(Packet p) throws Exception {
      if (p instanceof ClientboundGameProfilePacket) {
         this.loginPhase = false;
         return p;
      } else {
         ClientboundCustomPayloadPacket packet;
         String url;
         if (p instanceof ClientboundCustomPayloadPacket) {
            packet = (ClientboundCustomPayloadPacket)p;
            if (Restrictions.PLUGIN_CHANNEL.equals(packet.getIdentifier())) {
               url = this.replayHandler.getRestrictions().handle(packet);
               if (url == null) {
                  return null;
               }
               String url1 = url;
               this.terminateReplay();
               ReplayMod.instance.runLater(() -> {
                  try {
                     this.replayHandler.endReplay();
                  } catch (IOException var3) {
                     var3.printStackTrace();
                  }

                  this.mc.setScreen(new AlertScreen(() -> {
                     this.mc.setScreen(null);
                  }, Component.translatable("replaymod.error.unknownrestriction1"), Component.translatable("replaymod.error.unknownrestriction2", url1)));
               });
            }
         }

         if (p instanceof ClientboundDisconnectPacket) {
            Component reason = ((ClientboundDisconnectPacket)p).getReason();
            url = reason.getString();
            if ("Please update to view this replay.".equals(url)) {
               return null;
            }
         }

         if (BAD_PACKETS.contains(p.getClass())) {
            return null;
         } else {
            if (p instanceof ClientboundCustomPayloadPacket) {
               packet = (ClientboundCustomPayloadPacket)p;
               ResourceLocation channelName = packet.getIdentifier();
               String channelNameStr = channelName.toString();
               if (channelNameStr.startsWith("fabric-screen-handler-api-v")) {
                  return null;
               }
            }

            if (p instanceof ClientboundResourcePackPacket) {
               ClientboundResourcePackPacket clientboundResourcePackPacket = (ClientboundResourcePackPacket)p;
               url = clientboundResourcePackPacket.getUrl();
               if (url.startsWith("replay://")) {
                  int id = Integer.parseInt(url.substring("replay://".length()));
                  Map<Integer, String> index = this.replayFile.getResourcePackIndex();
                  if (index != null) {
                     String hash = index.get(id);
                     if (hash != null) {
                        File file = new File(this.tempResourcePackFolder, hash + ".zip");
                        if (!file.exists()) {
                           IOUtils.copy(this.replayFile.getResourcePack(hash).get(), new FileOutputStream(file));
                        }

                        MCVer.setServerResourcePack(file);
                     }
                  }

                  return null;
               }
            }

            if (p instanceof ClientboundLoginPacket) {
               ClientboundLoginPacket clientboundLoginPacket = (ClientboundLoginPacket)p;
               int entId = clientboundLoginPacket.playerId();
               this.schedulePacketHandler(() -> {
                  this.allowMovement = true;
               });
               this.actualID = entId;
               entId = -1789435;
               p = new ClientboundLoginPacket(entId, clientboundLoginPacket.hardcore(), GameType.SPECTATOR, GameType.SPECTATOR, clientboundLoginPacket.levels(), clientboundLoginPacket.registryHolder(), clientboundLoginPacket.dimensionType(), clientboundLoginPacket.dimension(), clientboundLoginPacket.seed(), 0, clientboundLoginPacket.chunkRadius(), clientboundLoginPacket.simulationDistance(), clientboundLoginPacket.reducedDebugInfo(), clientboundLoginPacket.showDeathScreen(), clientboundLoginPacket.isDebug(), clientboundLoginPacket.isFlat(), Optional.empty(), clientboundLoginPacket.portalCooldown());
            }

            if (p instanceof ClientboundRespawnPacket respawn) {
               p = new ClientboundRespawnPacket(respawn.getDimensionType(), respawn.getDimension(), respawn.getSeed(), GameType.SPECTATOR, GameType.SPECTATOR, respawn.isDebug(), respawn.isFlat(), (byte)0, Optional.empty(), respawn.getPortalCooldown());
               this.schedulePacketHandler(() -> {
                  this.allowMovement = true;
               });
            }

            if (p instanceof ClientboundPlayerPositionPacket ppl) {
               if (!this.hasWorldLoaded) {
                  this.hasWorldLoaded = true;
               }

               ReplayMod.instance.runLater(() -> {
                  if (this.mc.screen instanceof ReceivingLevelScreen) {
                     this.mc.setScreen(null);
                  }

               });
               if (this.replayHandler.shouldSuppressCameraMovements()) {
                  return null;
               } else {
                  Iterator var18 = ppl.getRelativeArguments().iterator();

                  RelativeMovement relative;
                  do {
                     if (!var18.hasNext()) {
                        this.schedulePacketHandler(new Runnable() {
                           public void run() {
                              if (FullReplaySender.this.mc.level != null && FullReplaySender.this.mc.isSameThread()) {
                                 CameraEntity cent = FullReplaySender.this.replayHandler.getCameraEntity();
                                 if (FullReplaySender.this.allowMovement || Math.abs(cent.getX() - ppl.getX()) > (double)FullReplaySender.TP_DISTANCE_LIMIT || Math.abs(cent.getZ() - ppl.getZ()) > (double)FullReplaySender.TP_DISTANCE_LIMIT) {
                                    FullReplaySender.this.allowMovement = false;
                                    cent.setCameraPosition(ppl.getX(), ppl.getY(), ppl.getZ());
                                    cent.setCameraRotation(ppl.getYRot(), ppl.getXRot(), cent.roll);
                                 }
                              } else {
                                 ReplayMod.instance.runLater(this);
                              }
                           }
                        });
                        return null;
                     }

                     relative = (RelativeMovement)var18.next();
                  } while(relative != RelativeMovement.X && relative != RelativeMovement.Y && relative != RelativeMovement.Z);

                  return null;
               }
            } else {
               if (p instanceof ClientboundGameEventPacket pg) {
                  if (!Arrays.asList(ClientboundGameEventPacket.START_RAINING, ClientboundGameEventPacket.STOP_RAINING, ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE).contains(pg.getEvent())) {
                     return null;
                  }
               }

               if ((p instanceof ClientboundSystemChatPacket || p instanceof ClientboundPlayerChatPacket || p instanceof ClientboundDisguisedChatPacket) && !(Boolean)ReplayModReplay.instance.getCore().getSettingsRegistry().get(Setting.SHOW_CHAT)) {
                  return null;
               } else if (this.asyncMode) {
                  return this.processPacketAsync(p);
               } else {
                  Packet pa = p;
                  this.mc.tell(() -> {
                     this.processPacketSync(pa);
                  });
                  return p;
               }
            }
         }
      }
   }

   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      this.ctx = ctx;
      super.channelActive(ctx);
   }

   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      promise.setSuccess();
   }

   public void flush(ChannelHandlerContext ctx) throws Exception {
   }

   public double getReplaySpeed() {
      return !this.paused() ? this.replaySpeed : 0.0D;
   }

   public void setReplaySpeed(double d) {
      if (d != 0.0D) {
         this.replaySpeed = d;
         this.realTimeStartSpeed = d;
         this.realTimeStart = System.currentTimeMillis() - (long)((double)this.lastTimeStamp / d);
      }

      TimerAccessor timer = (TimerAccessor)((MinecraftAccessor)this.mc).getTimer();
      timer.setTickLength(50.0F / (float)d);
   }

   public boolean isHurrying() {
      return this.desiredTimeStamp != -1L;
   }

   public void stopHurrying() {
      this.desiredTimeStamp = -1L;
   }

   public long getDesiredTimestamp() {
      return this.desiredTimeStamp;
   }

   public void jumpToTime(int millis) {
      Preconditions.checkState(this.asyncMode, "Can only jump in async mode. Use sendPacketsTill(int) instead.");
      if (millis < this.lastTimeStamp && !this.isHurrying()) {
         this.startFromBeginning = true;
      }

      this.desiredTimeStamp = millis;
   }

   protected Packet processPacketAsync(Packet p) {
      if (this.desiredTimeStamp - (long)this.lastTimeStamp > 1000L) {
         if (p instanceof ClientboundLevelParticlesPacket) {
            return null;
         }

         if (p instanceof ClientboundAddEntityPacket pso) {
            if (pso.getType() == EntityType.FIREWORK_ROCKET) {
               return null;
            }
         }
      }

      return p;
   }

   public void sendPacketsTill(int timestamp) {
      Preconditions.checkState(!this.asyncMode, "This method cannot be used in async mode. Use jumpToTime(int) instead.");
      AtomicBoolean doneSending = new AtomicBoolean();
      this.syncSender.submit(() -> {
         try {
            this.doSendPacketsTill(timestamp);
         } finally {
            doneSending.set(true);
         }

      });

      while(!doneSending.get()) {
         this.executeTaskQueue();

         try {
            Thread.sleep(0L, 100000);
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
            return;
         }
      }

      this.executeTaskQueue();
   }

   private void doSendPacketsTill(int timestamp) {
      while(true) {
         try {
            if (this.ctx == null && !this.terminate) {
               Thread.sleep(10L);
               continue;
            }

            synchronized(this) {
               if (timestamp == this.lastTimeStamp) {
                  return;
               }

               if (timestamp < this.lastTimeStamp) {
                  this.hasWorldLoaded = false;
                  this.lastTimeStamp = 0;
                  if (this.replayIn != null) {
                     this.replayIn.close();
                     this.replayIn = null;
                  }

                  this.loginPhase = true;
                  this.startFromBeginning = false;
                  this.nextPacket = null;
                  ReplayMod var10000 = ReplayMod.instance;
                  ReplayHandler var10001 = this.replayHandler;
                  Objects.requireNonNull(var10001);
                  var10000.runSync(var10001::restartedReplay);
               }

               if (this.replayIn == null) {
                  this.replayIn = this.replayFile.getPacketData(MCVer.getPacketTypeRegistry(true));
               }

               while(true) {
                  try {
                     FullReplaySender.PacketData pd;
                     if (this.nextPacket != null) {
                        pd = this.nextPacket;
                        this.nextPacket = null;
                     } else {
                        pd = new FullReplaySender.PacketData(this.replayIn, this.loginPhase);
                     }

                     int nextTimeStamp = pd.timestamp;
                     if (nextTimeStamp > timestamp) {
                        this.nextPacket = pd;
                        break;
                     }

                     this.channelRead(this.ctx, pd.bytes);
                  } catch (EOFException var6) {
                     this.replayIn = null;
                     break;
                  } catch (IOException var7) {
                     var7.printStackTrace();
                  }
               }

               this.realTimeStart = System.currentTimeMillis() - (long)((double)timestamp / this.replaySpeed);
               this.lastTimeStamp = timestamp;
            }
         } catch (Exception var9) {
            var9.printStackTrace();
         }

         return;
      }
   }

   private void executeTaskQueue() {
      ((MCVer.MinecraftMethodAccessor)this.mc).replayModExecuteTaskQueue();
      ReplayMod.instance.runTasks();
   }

   private void schedulePacketHandler(Runnable runnable) {
      if (this.mc.isSameThread()) {
         runnable.run();
      } else {
         this.mc.execute(runnable);
      }

   }

   protected void processPacketSync(Packet p) {
      if (p instanceof ClientboundForgetLevelChunkPacket packet) {
         int x = packet.getX();
         int z = packet.getZ();
         ClientLevel world = this.mc.level;
         ChunkSource chunkProvider = world.getChunkSource();
         LevelChunk chunk = chunkProvider.getChunkNow(x, z);
         if (chunk != null) {
            List<Entity> entitiesInChunk = new ArrayList();
            Iterator var9 = this.mc.level.entitiesForRendering().iterator();

            Entity entity;
            while(var9.hasNext()) {
               entity = (Entity)var9.next();
               if (entity.chunkPosition().equals(chunk.getPos())) {
                  entitiesInChunk.add(entity);
               }
            }

            var9 = entitiesInChunk.iterator();

            while(var9.hasNext()) {
               entity = (Entity)var9.next();
               this.forcePositionForVehicleAndSelf(entity);
            }
         }
      }

   }

   private void forcePositionForVehicleAndSelf(Entity entity) {
      Entity vehicle = entity.getVehicle();
      if (vehicle != null) {
         this.forcePositionForVehicleAndSelf(vehicle);
      }

      int var3 = 0;

      Vec3 prevPos;
      do {
         prevPos = entity.position();
         if (vehicle != null) {
            entity.rideTick();
         } else {
            entity.tick();
         }
      } while(prevPos.distanceToSqr(entity.position()) > 1.0E-4D && var3++ < 100);

   }

   private class EventHandler extends EventRegistrations {
      private EventHandler() {
         this.on(PreTickCallback.EVENT, this::onWorldTick);
      }

      private void onWorldTick() {
      }
   }

   private static final class PacketData {
      private static final com.github.steveice10.netty.buffer.ByteBuf byteBuf = com.github.steveice10.netty.buffer.Unpooled.buffer();
      private static final NetOutput netOutput;
      private final int timestamp;
      private final byte[] bytes;

      PacketData(ReplayInputStream in, boolean loginPhase) throws IOException {
         if (ReplayMod.isMinimalMode()) {
            this.timestamp = Utils.readInt(in);
            int length = Utils.readInt(in);
            if (this.timestamp == -1 || length == -1) {
               throw new EOFException();
            }

            this.bytes = new byte[length];
            IOUtils.readFully(in, this.bytes);
         } else {
            com.replaymod.replaystudio.PacketData data = in.readPacket();
            if (data == null) {
               throw new EOFException();
            }

            this.timestamp = (int)data.getTime();
            com.replaymod.replaystudio.protocol.Packet packet = data.getPacket();
            synchronized(byteBuf) {
               byteBuf.markReaderIndex();
               byteBuf.markWriterIndex();
               netOutput.writeVarInt(packet.getId());
               int idSize = byteBuf.readableBytes();
               int contentSize = packet.getBuf().readableBytes();
               this.bytes = new byte[idSize + contentSize];
               byteBuf.readBytes(this.bytes, 0, idSize);
               packet.getBuf().readBytes(this.bytes, idSize, contentSize);
               byteBuf.resetReaderIndex();
               byteBuf.resetWriterIndex();
            }

            packet.getBuf().release();
         }

      }

      static {
         netOutput = new ByteBufNetOutput(byteBuf);
      }
   }
}
