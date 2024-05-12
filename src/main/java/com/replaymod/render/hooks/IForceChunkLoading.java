package com.replaymod.render.hooks;

import net.minecraft.client.renderer.LevelRenderer;

public interface IForceChunkLoading {
   void replayModRender_setHook(ForceChunkLoadingHook var1);

   static IForceChunkLoading from(LevelRenderer worldRenderer) {
      return (IForceChunkLoading)worldRenderer;
   }
}
