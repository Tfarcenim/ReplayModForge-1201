package com.replaymod.render.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import com.replaymod.render.rendering.VideoRenderer;
import java.util.Iterator;

public interface ReplayRenderCallback {
   interface Post {
      Event<ReplayRenderCallback.Post> EVENT = Event.create((listeners) -> (renderer) -> {

         for (Post listener : listeners) {
            listener.afterRendering(renderer);
         }

      });

      void afterRendering(VideoRenderer var1);
   }

   interface Pre {
      Event<ReplayRenderCallback.Pre> EVENT = Event.create((listeners) -> (renderer) -> {

         for (Pre listener : listeners) {
            listener.beforeRendering(renderer);
         }

      });

      void beforeRendering(VideoRenderer var1);
   }
}
