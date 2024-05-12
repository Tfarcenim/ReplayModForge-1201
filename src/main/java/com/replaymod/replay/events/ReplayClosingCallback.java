package com.replaymod.replay.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.replay.ReplayHandler;
import java.io.IOException;
import java.util.Iterator;

public interface ReplayClosingCallback {
   Event<ReplayClosingCallback> EVENT = Event.create((listeners) -> {
      return (replayHandler) -> {

         for (ReplayClosingCallback listener : listeners) {
            listener.replayClosing(replayHandler);
         }

      };
   });

   void replayClosing(ReplayHandler var1) throws IOException;
}
