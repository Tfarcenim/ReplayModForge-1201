package com.replaymod.core.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface KeyEventCallback {
   Event<KeyEventCallback> EVENT = Event.create((listeners) -> {
      return (key, scanCode, action, modifiers) -> {
         Iterator<KeyEventCallback> var5 = listeners.iterator();

         KeyEventCallback listener;
         do {
            if (!var5.hasNext()) {
               return false;
            }

            listener = var5.next();
         } while(!listener.onKeyEvent(key, scanCode, action, modifiers));

         return true;
      };
   });
   int ACTION_RELEASE = 0;
   int ACTION_PRESS = 1;

   boolean onKeyEvent(int var1, int var2, int var3, int var4);
}
