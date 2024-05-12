package com.replaymod.render.hooks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface Texture2DStateCallback {
   Event<Texture2DStateCallback> EVENT = Event.create((listeners) -> (slot, enabled) -> {

      for (Texture2DStateCallback listener : listeners) {
         listener.texture2DStateChanged(slot, enabled);
      }

   });

   void texture2DStateChanged(int var1, boolean var2);
}
