package com.replaymod.core.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface PreRenderHandCallback {
   Event<PreRenderHandCallback> EVENT = Event.create((listeners) -> {
      return () -> {
         Iterator<PreRenderHandCallback> var1 = listeners.iterator();

         PreRenderHandCallback listener;
         do {
            if (!var1.hasNext()) {
               return false;
            }

            listener = var1.next();
         } while(!listener.preRenderHand());

         return true;
      };
   });

   boolean preRenderHand();
}
