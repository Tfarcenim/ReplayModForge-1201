package com.replaymod.mixin;

import com.replaymod.replay.ReplayModReplay;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({TippableArrowRenderer.class})
public abstract class MixinRenderArrow extends EntityRenderer {
   protected MixinRenderArrow(Context context) {
      super(context);
   }

   public boolean shouldRender(Entity entity, Frustum camera, double camX, double camY, double camZ) {
      return ReplayModReplay.instance.getReplayHandler() != null || super.shouldRender(entity, camera, camX, camY, camZ);
   }
}
