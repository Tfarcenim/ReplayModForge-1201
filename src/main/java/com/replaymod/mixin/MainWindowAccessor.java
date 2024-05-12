package com.replaymod.mixin;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Window.class})
public interface MainWindowAccessor {
   @Accessor
   int getFramebufferWidth();

   @Accessor
   void setFramebufferWidth(int var1);

   @Accessor
   int getFramebufferHeight();

   @Accessor
   void setFramebufferHeight(int var1);

   @Invoker("onFramebufferResize")
   void invokeOnFramebufferSizeChanged(long var1, int var3, int var4);
}
