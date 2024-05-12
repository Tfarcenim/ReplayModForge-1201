package com.replaymod.core.events;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;

public interface PostRenderCallback {
   Event<PostRenderCallback> EVENT = Event.create((listeners) -> () -> {

      for (PostRenderCallback listener : listeners) {
         listener.postRender();
      }

   });

   void postRender();
}
