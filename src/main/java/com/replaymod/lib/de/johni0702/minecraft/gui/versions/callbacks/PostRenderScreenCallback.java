package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;
import net.minecraft.client.gui.GuiGraphics;

public interface PostRenderScreenCallback {
   Event<PostRenderScreenCallback> EVENT = Event.create((listeners) -> (stack, partialTicks) -> {

      for (PostRenderScreenCallback listener : listeners) {
         listener.postRenderScreen(stack, partialTicks);
      }

   });

   void postRenderScreen(GuiGraphics var1, float var2);
}
