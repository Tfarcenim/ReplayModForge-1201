package com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Event;
import java.util.Iterator;

public interface MouseCallback {
   Event<MouseCallback> EVENT = Event.create((listeners) -> {
      return new MouseCallback() {
         public boolean mouseDown(double x, double y, int button) {
            Iterator<MouseCallback> var6 = listeners.iterator();

            MouseCallback listener;
            do {
               if (!var6.hasNext()) {
                  return false;
               }

               listener = var6.next();
            } while(!listener.mouseDown(x, y, button));

            return true;
         }

         public boolean mouseDrag(double x, double y, int button, double dx, double dy) {
            Iterator<MouseCallback> var10 = listeners.iterator();

            MouseCallback listener;
            do {
               if (!var10.hasNext()) {
                  return false;
               }

               listener = var10.next();
            } while(!listener.mouseDrag(x, y, button, dx, dy));

            return true;
         }

         public boolean mouseUp(double x, double y, int button) {
            Iterator var6 = listeners.iterator();

            MouseCallback listener;
            do {
               if (!var6.hasNext()) {
                  return false;
               }

               listener = (MouseCallback)var6.next();
            } while(!listener.mouseUp(x, y, button));

            return true;
         }

         public boolean mouseScroll(double x, double y, double scroll) {
            Iterator var7 = listeners.iterator();

            MouseCallback listener;
            do {
               if (!var7.hasNext()) {
                  return false;
               }

               listener = (MouseCallback)var7.next();
            } while(!listener.mouseScroll(x, y, scroll));

            return true;
         }
      };
   });

   boolean mouseDown(double var1, double var3, int var5);

   boolean mouseDrag(double var1, double var3, int var5, double var6, double var8);

   boolean mouseUp(double var1, double var3, int var5);

   boolean mouseScroll(double var1, double var3, double var5);
}
