package com.replaymod.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ClientPacketListener.class})
public class Mixin_FixEntityNotTracking {
   @ModifyVariable(
      method = {"handleTeleportEntity", "handleMoveEntity", "handleSetEntityPassengersPacket"},
      at = @At("RETURN"),
      ordinal = 0
   )
   private Entity updatePositionIfNotTracked$0(Entity entity) {
      if (entity != null) {
         entity.getSelfAndPassengers().forEach(this::updatePositionIfNotTracked);
      }

      return entity;
   }

   private void updatePositionIfNotTracked(Entity entity) {
      if (entity != null) {
         Level var3 = entity.level();
         if (var3 instanceof ClientWorldAccessor) {
            ClientWorldAccessor world = (ClientWorldAccessor)var3;
            if (!world.getTickingEntities().contains(entity)) {
               if (entity.isControlledByLocalInstance()) {
                  return;
               }

               int var5 = 0;

               Vec3 prevPos;
               do {
                  prevPos = entity.position();
                  if (entity.isPassenger()) {
                     entity.rideTick();
                  } else {
                     entity.tick();
                  }
               } while(prevPos.distanceToSqr(entity.position()) > 1.0E-4D && var5++ < 100);
            }
         }
      }
   }
}
