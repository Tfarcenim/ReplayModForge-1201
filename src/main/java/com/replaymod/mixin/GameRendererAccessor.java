package com.replaymod.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({GameRenderer.class})
public interface GameRendererAccessor {
   @Accessor
   boolean getRenderHand();

   @Accessor
   void setRenderHand(boolean var1);
}
