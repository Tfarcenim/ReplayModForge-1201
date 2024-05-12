package com.replaymod.render.hooks;

import com.replaymod.render.gui.progress.VirtualWindow;
import net.minecraft.client.Minecraft;

public interface MinecraftClientExt {
   void setWindowDelegate(VirtualWindow var1);

   static MinecraftClientExt get(Minecraft mc) {
      return (MinecraftClientExt)mc;
   }
}
