package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collection;

public interface InitScreenCallback {
   Event<InitScreenCallback> EVENT = Event.create((listeners) -> (screen, buttons) -> {

      for (InitScreenCallback listener : listeners) {
         listener.initScreen(screen, buttons);
      }

   });

   void initScreen(Screen var1, Collection<AbstractWidget> var2);

   interface Pre {
      Event<InitScreenCallback.Pre> EVENT = Event.create((listeners) -> (screen) -> {

         for (Pre listener : listeners) {
            listener.preInitScreen(screen);
         }

      });

      void preInitScreen(Screen var1);
   }
}
