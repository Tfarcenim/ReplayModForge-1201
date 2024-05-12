package com.replaymod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.core.events.PostRenderWorldCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class MixinGameRenderer {

   //todo why doesnt renderstagevent work?
   @Inject(method = { "renderLevel" }, at = {
           @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z") })
   private void postRenderWorld(float partialTicks, long nanoTime, PoseStack matrixStack, CallbackInfo ci) {
      PostRenderWorldCallback.EVENT.invoker().postRenderWorld(matrixStack);
   }

   @Inject(
      method = {"renderItemInHand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preRenderHand(PoseStack matrixStack, Camera camera, float partialTicks, CallbackInfo ci) {
      if (PreRenderHandCallback.EVENT.invoker().preRenderHand()) {
         ci.cancel();
      }
   }
}
