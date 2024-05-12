package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiNumberField<T extends IGuiNumberField<T>> extends IGuiTextField<T> {
   byte getByte();

   short getShort();

   int getInteger();

   long getLong();

   float getFloat();

   double getDouble();

   T setValue(int var1);

   T setValue(double var1);

   T setMinValue(Double var1);

   T setMaxValue(Double var1);

   T setMinValue(int var1);

   T setMaxValue(int var1);

   T setValidateOnFocusChange(boolean var1);

   T setPrecision(int var1);
}
