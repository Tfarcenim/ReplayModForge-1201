package com.replaymod.mixin;

import java.util.Queue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ParticleEngine.class})
public abstract class MixinParticleManager2 {
   @Final
   @Shadow
   private Queue<Particle> particlesToAdd;

   @Inject(
      method = {"setLevel"},
      at = {@At("HEAD")}
   )
   public void replayModReplay_clearParticleQueue(ClientLevel world, CallbackInfo ci) {
      this.particlesToAdd.clear();
   }
}
