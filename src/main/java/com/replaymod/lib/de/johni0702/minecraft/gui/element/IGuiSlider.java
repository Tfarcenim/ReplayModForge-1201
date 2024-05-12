package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiSlider<T extends IGuiSlider<T>> extends GuiElement<T> {
   T setText(String var1);

   T setI18nText(String var1, Object... var2);

   T setValue(int var1);

   int getValue();

   int getSteps();

   T setSteps(int var1);

   T onValueChanged(Runnable var1);
}
