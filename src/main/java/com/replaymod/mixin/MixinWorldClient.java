package com.replaymod.mixin;

import com.replaymod.recording.handler.RecordingEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientLevel.class})
public abstract class MixinWorldClient extends Level implements RecordingEventHandler.RecordingEventSender {
   @Final
   @Shadow
   private Minecraft minecraft;

   protected MixinWorldClient() {
      super(null, null, null, null, null, false, false, 0L, 0);
   }

   private RecordingEventHandler replayModRecording_getRecordingEventHandler() {
      return ((RecordingEventHandler.RecordingEventSender)this.minecraft.levelRenderer).getRecordingEventHandler();
   }

   @Inject(
      method = {"playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"},
      at = {@At("HEAD")}
   )
   public void replayModRecording_recordClientSound(Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed, CallbackInfo ci) {
      if (player == this.minecraft.player) {
         RecordingEventHandler handler = this.replayModRecording_getRecordingEventHandler();
         if (handler != null) {
            handler.onPacket(new ClientboundSoundPacket(sound, category, x, y, z, volume, pitch, seed));
         }
      }

   }

   @Inject(
      method = {"levelEvent"},
      at = {@At("HEAD")}
   )
   private void playLevelEvent(Player player, int type, BlockPos pos, int data, CallbackInfo ci) {
      if (player == this.minecraft.player) {
         RecordingEventHandler handler = this.replayModRecording_getRecordingEventHandler();
         if (handler != null) {
            handler.onClientEffect(type, pos, data);
         }
      }

   }
}
