package com.replaymod.replay.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;
import java.io.IOException;
import java.util.Iterator;

public interface ReplayOpenedCallback {
   Event<ReplayOpenedCallback> EVENT = Event.create((listeners) -> (replayHandler) -> {

      for (ReplayOpenedCallback listener : listeners) {
         listener.replayOpened(replayHandler);
      }

   });

   void replayOpened(ReplayHandler var1) throws IOException;
}
