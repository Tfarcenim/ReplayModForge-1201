package com.replaymod.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({GameRenderer.class})
public abstract class Mixin_PreserveDepthDuringHandRendering {
   @ModifyArg(
      method = {"renderLevel"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
),
      index = 0
   )
   private int replayModRender_skipClearWhenRecordingDepth(int mask) {
      EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer)this).replayModRender_getHandler();
      if (handler != null && handler.getSettings().isDepthMap()) {
         mask &= ~0b100000000;
      }

      return mask;
   }
}
