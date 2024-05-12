package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiCheckbox<T extends IGuiCheckbox<T>> extends IGuiClickable<T> {
   T setLabel(String var1);

   T setI18nLabel(String var1, Object... var2);

   T setChecked(boolean var1);

   String getLabel();

   boolean isChecked();
}
