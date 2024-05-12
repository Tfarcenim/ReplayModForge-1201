package com.replaymod.mixin;

import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({GameRenderer.class})
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
   private static final String METHOD = "getProjectionMatrix";
   private static final String TARGET = "Lorg/joml/Matrix4f;setPerspective(FFFF)Lorg/joml/Matrix4f;";
   private static final boolean TARGET_REMAP = false;
   private static final float OMNIDIRECTIONAL_FOV = 1.5707964F;

   @ModifyArg(
      method = METHOD,
      at = @At(
   value = "INVOKE",
   target = TARGET,
   remap = false
),
      index = 0
   )
   private float replayModRender_perspective_fov(float fovY) {
      return this.isOmnidirectional() ? 1.5707964F : fovY;
   }

   @ModifyArg(
      method = METHOD,
      at = @At(
   value = "INVOKE",
   target = TARGET,
   remap = false
),
      index = 1
   )
   private float replayModRender_perspective_aspect(float aspect) {
      return this.isOmnidirectional() ? 1.0F : aspect;
   }

   @Unique
   private boolean isOmnidirectional() {
      return this.replayModRender_getHandler() != null && this.replayModRender_getHandler().omnidirectional;
   }
}
