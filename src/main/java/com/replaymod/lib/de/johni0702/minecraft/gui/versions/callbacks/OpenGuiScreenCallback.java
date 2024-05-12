package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;
import net.minecraft.client.gui.screens.Screen;

public interface OpenGuiScreenCallback {
   Event<OpenGuiScreenCallback> EVENT = Event.create((listeners) -> (screen) -> {

      for (OpenGuiScreenCallback listener : listeners) {
         listener.openGuiScreen(screen);
      }

   });

   void openGuiScreen(Screen var1);
}
