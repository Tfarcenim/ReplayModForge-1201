package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.compat.ForgeEventAdapter;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class EventRegistrations {

   static {
      new ForgeEventAdapter().register();
   }

   private final List<EventRegistration<?>> registrations = new ArrayList<>();

   public <T> EventRegistrations on(EventRegistration<T> registration) {
      this.registrations.add(registration);
      return this;
   }

   public <T> EventRegistrations on(Event<T> event, T listener) {
      return this.on(EventRegistration.create(event, listener));
   }

   public void register() {
      MinecraftForge.EVENT_BUS.register(this);
      for (EventRegistration<?> eventRegistration : this.registrations) {
         eventRegistration.register();
      }

   }

   public void unregister() {
      MinecraftForge.EVENT_BUS.unregister(this);
      for (EventRegistration<?> eventRegistration : this.registrations) {
         eventRegistration.unregister();
      }
   }
}
