package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;
import net.minecraft.client.gui.GuiGraphics;

public interface RenderHudCallback {
   Event<RenderHudCallback> EVENT = Event.create((listeners) -> (stack, partialTicks) -> {

      for (RenderHudCallback listener : listeners) {
         listener.renderHud(stack, partialTicks);
      }

   });

   void renderHud(GuiGraphics var1, float var2);
}
