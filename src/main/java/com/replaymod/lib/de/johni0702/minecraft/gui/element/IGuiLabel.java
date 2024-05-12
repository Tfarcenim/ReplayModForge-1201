package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiLabel<T extends IGuiLabel<T>> extends GuiElement<T> {
   T setText(String var1);

   T setI18nText(String var1, Object... var2);

   T setColor(ReadableColor var1);

   T setDisabledColor(ReadableColor var1);

   String getText();

   ReadableColor getColor();

   ReadableColor getDisabledColor();
}
