package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiTextArea<T extends IGuiTextArea<T>> extends GuiElement<T>, Focusable<T> {
   T setText(String[] var1);

   String[] getText();

   String getText(int var1, int var2, int var3, int var4);

   int getSelectionFromX();

   int getSelectionToX();

   int getSelectionFromY();

   int getSelectionToY();

   String getSelectedText();

   void deleteSelectedText();

   String cutSelectedText();

   void writeText(String var1);

   void writeChar(char var1);

   T setCursorPosition(int var1, int var2);

   T setMaxTextWidth(int var1);

   T setMaxTextHeight(int var1);

   T setMaxCharCount(int var1);

   T setTextColor(ReadableColor var1);

   T setTextColorDisabled(ReadableColor var1);

   int getMaxTextWidth();

   int getMaxTextHeight();

   int getMaxCharCount();

   String[] getHint();

   T setHint(String... var1);

   T setI18nHint(String var1, Object... var2);
}
