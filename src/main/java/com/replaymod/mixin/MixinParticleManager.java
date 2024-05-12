package com.replaymod.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ParticleEngine.class})
public abstract class MixinParticleManager {
   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"
)
   )
   private void buildOrientedGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
      EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer)MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
      if (handler != null && handler.omnidirectional) {
         Quaternionf rotation = camera.rotation();
         Quaternionf org = new Quaternionf(rotation);

         try {
            Vec3 from = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 to = MCVer.getPosition(particle, partialTicks).subtract(camera.getPosition()).normalize();
            Vec3 axis = from.cross(to);
            rotation.set((float)axis.x, (float)axis.y, (float)axis.z, (float)(1.0D + from.dot(to)));
            rotation.normalize();
            this.buildGeometry(particle, vertexConsumer, camera, partialTicks);
         } finally {
            rotation.set(org.w, org.x, org.y, org.z);
         }
      } else {
         this.buildGeometry(particle, vertexConsumer, camera, partialTicks);
      }

   }

   private void buildGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
      particle.render(vertexConsumer, camera, partialTicks);
   }
}
