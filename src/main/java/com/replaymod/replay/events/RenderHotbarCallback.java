package com.replaymod.replay.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface RenderHotbarCallback {
   Event<RenderHotbarCallback> EVENT = Event.create((listeners) -> {
      return () -> {
         Iterator var1 = listeners.iterator();

         Boolean state;
         do {
            if (!var1.hasNext()) {
               return null;
            }

            RenderHotbarCallback listener = (RenderHotbarCallback)var1.next();
            state = listener.shouldRenderHotbar();
         } while(state == null);

         return state;
      };
   });

   Boolean shouldRenderHotbar();
}
