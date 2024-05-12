package com.replaymod.render.hooks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface FogStateCallback {
   Event<FogStateCallback> EVENT = Event.create((listeners) -> (enabled) -> {

      for (FogStateCallback listener : listeners) {
         listener.fogStateChanged(enabled);
      }

   });

   void fogStateChanged(boolean var1);
}
