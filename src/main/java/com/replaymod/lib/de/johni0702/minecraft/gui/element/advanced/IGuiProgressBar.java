package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;

public interface IGuiProgressBar<T extends IGuiProgressBar<T>> extends GuiElement<T> {
   T setProgress(float var1);

   T setLabel(String var1);

   T setI18nLabel(String var1, Object... var2);

   float getProgress();

   String getLabel();
}
