package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiHorizontalScrollbar<T extends IGuiHorizontalScrollbar<T>> extends GuiElement<T> {
   T setPosition(double var1);

   double getPosition();

   T setZoom(double var1);

   double getZoom();

   T onValueChanged(Runnable var1);
}
