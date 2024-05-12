package com.replaymod.recording.handler;

import com.mojang.datafixers.util.Pair;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.mixin.IntegratedServerAccessor;
import com.replaymod.recording.packet.PacketListener;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.PosRot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RecordingEventHandler extends EventRegistrations {
   private final Minecraft mc = MCVer.getMinecraft();
   private final PacketListener packetListener;
   private Double lastX;
   private Double lastY;
   private Double lastZ;
   private final List<ItemStack> playerItems;
   private int ticksSinceLastCorrection;
   private boolean wasSleeping;
   private int lastRiding;
   private Integer rotationYawHeadBefore;

   public RecordingEventHandler(PacketListener packetListener) {
      this.playerItems = NonNullList.withSize(6, ItemStack.EMPTY);
      this.lastRiding = -1;
      this.on(PreTickCallback.EVENT, this::onPlayerTick);
      this.on(PreRenderCallback.EVENT, this::checkForGamePaused);
      this.packetListener = packetListener;
   }

   public void register() {
      super.register();
      ((RecordingEventHandler.RecordingEventSender)this.mc.levelRenderer).setRecordingEventHandler(this);
   }

   public void unregister() {
      super.unregister();
      RecordingEventHandler.RecordingEventSender recordingEventSender = (RecordingEventHandler.RecordingEventSender)this.mc.levelRenderer;
      if (recordingEventSender.getRecordingEventHandler() == this) {
         recordingEventSender.setRecordingEventHandler(null);
      }

   }

   public void onPacket(Packet<?> packet) {
      this.packetListener.save(packet);
   }

   public void spawnRecordingPlayer() {
      try {
         LocalPlayer player = this.mc.player;

         assert player != null;

         this.packetListener.save(new ClientboundAddPlayerPacket(player));
         this.packetListener.save(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData().getNonDefaultValues()));
         this.lastX = this.lastY = this.lastZ = null;
         this.playerItems.clear();
         this.lastRiding = -1;
         this.wasSleeping = false;
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public void onClientEffect(int type, BlockPos pos, int data) {
      try {
         this.packetListener.save(new ClientboundLevelEventPacket(type, pos, data, false));
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private void onPlayerTick() {
      if (this.mc.player != null) {
         LocalPlayer player = this.mc.player;

         try {
            boolean force = false;
            if (this.lastX == null || this.lastY == null || this.lastZ == null) {
               force = true;
               this.lastX = player.getX();
               this.lastY = player.getY();
               this.lastZ = player.getZ();
            }

            ++this.ticksSinceLastCorrection;
            if (this.ticksSinceLastCorrection >= 100) {
               this.ticksSinceLastCorrection = 0;
               force = true;
            }

            double dx = player.getX() - this.lastX;
            double dy = player.getY() - this.lastY;
            double dz = player.getZ() - this.lastZ;
            this.lastX = player.getX();
            this.lastY = player.getY();
            this.lastZ = player.getZ();
            double maxRelDist = 8.0D;
            Packet<?> packet;
            if (!force && !(Math.abs(dx) > 8.0D) && !(Math.abs(dy) > 8.0D) && !(Math.abs(dz) > 8.0D)) {
               byte newYaw = (byte)((int)(player.getYRot() * 256.0F / 360.0F));
               byte newPitch = (byte)((int)(player.getXRot() * 256.0F / 360.0F));
               packet = new PosRot(player.getId(), (short)((int)Math.round(dx * 4096.0D)), (short)((int)Math.round(dy * 4096.0D)), (short)((int)Math.round(dz * 4096.0D)), newYaw, newPitch, player.onGround());
            } else {
               packet = new ClientboundTeleportEntityPacket(player);
            }

            this.packetListener.save(packet);
            int rotationYawHead = (int)(player.yHeadRot * 256.0F / 360.0F);
            if (!Objects.equals(rotationYawHead, this.rotationYawHeadBefore)) {
               this.packetListener.save(new ClientboundRotateHeadPacket(player, (byte)rotationYawHead));
               this.rotationYawHeadBefore = rotationYawHead;
            }

            this.packetListener.save(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
            if (player.swinging && player.swingTime == 0) {
               this.packetListener.save(new ClientboundAnimatePacket(player, player.swingingArm == InteractionHand.MAIN_HAND ? 0 : 3));
            }

            EquipmentSlot[] var21 = EquipmentSlot.values();
            int vehicleId = var21.length;

            for(int var15 = 0; var15 < vehicleId; ++var15) {
               EquipmentSlot slot = var21[var15];
               ItemStack stack = player.getItemBySlot(slot);
               int index = slot.ordinal();
               if (!ItemStack.matches(this.playerItems.get(index), stack)) {
                  stack = stack.copy();
                  this.playerItems.set(index, stack);
                  this.packetListener.save(new ClientboundSetEquipmentPacket(player.getId(), Collections.singletonList(Pair.of(slot, stack))));
               }
            }

            Entity vehicle = player.getVehicle();
            vehicleId = vehicle == null ? -1 : vehicle.getId();
            if (this.lastRiding != vehicleId) {
               this.lastRiding = vehicleId;
               this.packetListener.save(new ClientboundSetEntityLinkPacket(player, vehicle));
            }

            if (!player.isSleeping() && this.wasSleeping) {
               this.packetListener.save(new ClientboundAnimatePacket(player, 2));
               this.wasSleeping = false;
            }
         } catch (Exception var19) {
            var19.printStackTrace();
         }

      }
   }

   public void onBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
      Player thePlayer = this.mc.player;
      if (thePlayer != null && breakerId == thePlayer.getId()) {
         this.packetListener.save(new ClientboundBlockDestructionPacket(breakerId, pos, progress));
      }

   }

   private void checkForGamePaused() {
      if (this.mc.hasSingleplayerServer()) {
         IntegratedServer server = this.mc.getSingleplayerServer();
         if (server != null && ((IntegratedServerAccessor)server).isGamePaused()) {
            this.packetListener.setServerWasPaused();
         }
      }

   }

   public interface RecordingEventSender {
      void setRecordingEventHandler(RecordingEventHandler var1);

      RecordingEventHandler getRecordingEventHandler();
   }
}
