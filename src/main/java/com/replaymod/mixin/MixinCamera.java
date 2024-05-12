package com.replaymod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class MixinCamera {
   @Shadow
   @Final
   Minecraft minecraft;

   @Inject(
      method = {"renderLevel"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Camera;getXRot()F"
)}
   )
   private void applyRoll(float float_1, long long_1, PoseStack matrixStack, CallbackInfo ci) {
      Entity entity = this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity();
      if (entity instanceof CameraEntity) {
         matrixStack.mulPose((new Quaternionf()).fromAxisAngleDeg(new Vector3f(0.0F, 0.0F, 1.0F), ((CameraEntity)entity).roll));
      }

   }
}
