package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiTimeline<T extends IGuiTimeline<T>> extends GuiElement<T> {
   T setLength(int var1);

   int getLength();

   T setCursorPosition(int var1);

   int getCursorPosition();

   T setZoom(double var1);

   double getZoom();

   T setOffset(int var1);

   int getOffset();

   T setMarkers();

   T setMarkers(boolean var1);

   boolean getMarkers();

   int getMarkerInterval();

   T setCursor(boolean var1);

   boolean getCursor();

   T onClick(IGuiTimeline.OnClick var1);

   public interface OnClick {
      void run(int var1);
   }
}
