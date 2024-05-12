package com.replaymod.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({GameRenderer.class})
public abstract class Mixin_PreserveDepthDuringGuiRendering {
   @ModifyArg(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
),
      index = 0
   )
   private int replayModRender_skipClearWhenRecordingDepth(int mask) {
      EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer)Minecraft.getInstance().gameRenderer).replayModRender_getHandler();
      if (handler != null && handler.getSettings().isDepthMap()) {
         mask &= -257;
      }

      return mask;
   }
}
