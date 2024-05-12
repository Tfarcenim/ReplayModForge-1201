package com.replaymod.mixin;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.ScreenExt;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Screen.class})
public abstract class Mixin_PassEventsExt implements ScreenExt {
   @Unique
   private boolean passEvents;

   public boolean doesPassEvents() {
      return this.passEvents;
   }

   public void setPassEvents(boolean passEvents) {
      this.passEvents = passEvents;
   }
}
