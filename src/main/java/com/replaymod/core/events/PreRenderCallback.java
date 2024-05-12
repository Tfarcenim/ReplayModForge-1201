package com.replaymod.core.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface PreRenderCallback {
   Event<PreRenderCallback> EVENT = Event.create((listeners) -> () -> {

      for (PreRenderCallback listener : listeners) {
         listener.preRender();
      }

   });

   void preRender();
}
