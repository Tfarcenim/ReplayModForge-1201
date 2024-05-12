package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.ComposedGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import java.util.Comparator;
import java.util.Map;

public interface GuiContainer<T extends GuiContainer<T>> extends ComposedGuiElement<T> {
   T setLayout(Layout var1);

   Layout getLayout();

   void convertFor(GuiElement var1, Point var2);

   void convertFor(GuiElement var1, Point var2, int var3);

   Map<GuiElement, LayoutData> getElements();

   T addElements(LayoutData var1, GuiElement... var2);

   T removeElement(GuiElement var1);

   T sortElements();

   T sortElements(Comparator<GuiElement> var1);

   ReadableColor getBackgroundColor();

   T setBackgroundColor(ReadableColor var1);
}
