package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import java.util.Map;
import java.util.function.Function;

public interface IGuiDropdownMenu<V, T extends IGuiDropdownMenu<V, T>> extends GuiElement<T> {
   T setValues(V... var1);

   T setSelected(int var1);

   T setSelected(V var1);

   V getSelectedValue();

   T setOpened(boolean var1);

   int getSelected();

   V[] getValues();

   boolean isOpened();

   T onSelection(Consumer<Integer> var1);

   Map<V, IGuiClickable> getDropdownEntries();

   T setToString(Function<V, String> var1);
}
