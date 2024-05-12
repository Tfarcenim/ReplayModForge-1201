package com.replaymod.replay.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;
import java.util.Iterator;

public interface ReplayClosedCallback {
   Event<ReplayClosedCallback> EVENT = Event.create((listeners) -> {
      return (replayHandler) -> {

         for (ReplayClosedCallback listener : listeners) {
            listener.replayClosed(replayHandler);
         }

      };
   });

   void replayClosed(ReplayHandler var1);
}
