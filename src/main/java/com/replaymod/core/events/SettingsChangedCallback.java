package com.replaymod.core.events;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface SettingsChangedCallback {
   Event<SettingsChangedCallback> EVENT = Event.create((listeners) -> (registry, key) -> {

      for (SettingsChangedCallback listener : listeners) {
         listener.onSettingsChanged(registry, key);
      }

   });

   void onSettingsChanged(SettingsRegistry var1, SettingsRegistry.SettingKey<?> var2);
}
