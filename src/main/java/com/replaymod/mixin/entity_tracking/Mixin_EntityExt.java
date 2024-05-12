package com.replaymod.mixin.entity_tracking;

import com.replaymod.replay.ext.EntityExt;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Entity.class})
public abstract class Mixin_EntityExt implements EntityExt {
   @Shadow
   private float yRot;
   @Shadow
   private float xRot;
   @Unique
   private float trackedYaw = Float.NaN;
   @Unique
   private float trackedPitch = Float.NaN;

   public float replaymod$getTrackedYaw() {
      return !Float.isNaN(this.trackedYaw) ? this.trackedYaw : this.yRot;
   }

   public float replaymod$getTrackedPitch() {
      return !Float.isNaN(this.trackedPitch) ? this.trackedPitch : this.xRot;
   }

   public void replaymod$setTrackedYaw(float value) {
      this.trackedYaw = value;
   }

   public void replaymod$setTrackedPitch(float value) {
      this.trackedPitch = value;
   }
}
