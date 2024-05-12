package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.NonNull;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

public interface IGuiTextField<T extends IGuiTextField<T>> extends GuiElement<T>, Focusable<T> {
   @NonNull
   T setText(String var1);

   @NonNull
   T setI18nText(String var1, Object... var2);

   @NonNull
   String getText();

   int getMaxLength();

   T setMaxLength(int var1);

   @NonNull
   String deleteText(int var1, int var2);

   int getSelectionFrom();

   int getSelectionTo();

   @NonNull
   String getSelectedText();

   @NonNull
   String deleteSelectedText();

   @NonNull
   T writeText(String var1);

   @NonNull
   T writeChar(char var1);

   T deleteNextChar();

   String deleteNextWord();

   @NonNull
   T deletePreviousChar();

   @NonNull
   String deletePreviousWord();

   @NonNull
   T setCursorPosition(int var1);

   T onEnter(Runnable var1);

   T onTextChanged(Consumer<String> var1);

   String getHint();

   T setHint(String var1);

   T setI18nHint(String var1, Object... var2);

   ReadableColor getTextColor();

   T setTextColor(ReadableColor var1);

   ReadableColor getTextColorDisabled();

   T setTextColorDisabled(ReadableColor var1);
}
