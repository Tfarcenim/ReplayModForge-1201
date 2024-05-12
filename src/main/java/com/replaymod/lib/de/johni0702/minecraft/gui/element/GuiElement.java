package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.Minecraft;

public interface GuiElement<T extends GuiElement<T>> {
   Minecraft getMinecraft();

   GuiContainer getContainer();

   T setContainer(GuiContainer var1);

   void layout(ReadableDimension var1, RenderInfo var2);

   void draw(GuiRenderer var1, ReadableDimension var2, RenderInfo var3);

   ReadableDimension getMinSize();

   ReadableDimension getMaxSize();

   T setMaxSize(ReadableDimension var1);

   boolean isEnabled();

   T setEnabled(boolean var1);

   T setEnabled();

   T setDisabled();

   GuiElement getTooltip(RenderInfo var1);

   T setTooltip(GuiElement var1);

   int getLayer();
}
