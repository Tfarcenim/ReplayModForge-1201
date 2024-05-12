package com.replaymod.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Particle.class})
public interface ParticleAccessor {
   @Accessor
   double getXo();

   @Accessor
   double getYo();

   @Accessor
   double getZo();

   @Accessor("x")
   double getPosX();

   @Accessor("y")
   double getPosY();

   @Accessor("z")
   double getPosZ();
}
